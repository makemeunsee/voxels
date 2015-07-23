function getURLParameter(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) {
            return sParameterName[1];
        }
    }
}

function appMain() {

    var scalaObj = demo.webapp.VoxelMain();

    // help dialog
    $(function() {
        $( "#dialog" ).dialog({
            autoOpen: false,
            width: 600,
            height: 222
        });
    });

    var uiVisible = true;
    function toggleUI( show ) {
        if ( show === undefined ) {
            show = !uiVisible;
        }
        if ( show ) {
            $( ".gui" ).show();
            $( ".dg" ).show();
            stats.domElement.style.display = "block";
        } else {
            $( ".gui" ).hide();
            $( ".dg" ).hide();
            stats.domElement.style.display = "none";
        }
        uiVisible = show;
    }

    // jqueryui widgets
    $(function() {
      $( "button" ).button();
    });

    function showHelp() {
        if ( $( "#dialog" ).dialog( "isOpen" ) )
            $( "#dialog" ).dialog( "close" );
        else
            $( "#dialog" ).dialog( "open" );
    }
    $("#showHelp").unbind("click");
    $("#showHelp").click(showHelp);

    $("#reset").unbind("click");
    $("#reset").click(function() {
        window.location = window.location.pathname;
    });

    var takeScreenshot = false;
    function screenshot() {
        takeScreenshot = true;
    }
    $("#screenshot").unbind("click");
    $("#screenshot").click(screenshot);

    var exportToMegaText = false;
    function megaExport() {
        exportToMegaText = true;
    }
    $("#megaText").unbind("click");
    $("#megaText").click(megaExport);

    var toggleFullscreen = THREEx.FullScreen.toggleFct();
    $("#fullscreen").unbind("click");
    $("#fullscreen").click(toggleFullscreen);

    $( "#progressbar" ).progressbar( { value: false,
                                       change: function() {},
                                       complete: function() {}
                                     } );

    var mainContainer = $( "#main" )[0];

    var updateProjection = function(screenWidth, screenHeight) {
        scalaObj.scene.updateViewport( screenWidth, screenHeight );
    };

    function leftButton(evt) {
        var button = evt.which || evt.button;
        return button == 1;
    }

    var mx, my = 0;

    var tapped = false; // double tap detection

    function onMouseDown(event) {
        if (leftButton(event)) {
            event.touches = [{clientX: event.clientX, clientY: event.clientY}];
            onTouchStart(event);
            clearTimeout(tapped);
            tapped = null; // double click is handled natively
        }
    }

    var clicked = false;
    var dragging = false;

    // only react to left clicks
    function onTouchStart(event) {
        // dont handle multi touch
        if (event.touches.length === 1) {
            mx = event.touches[0].clientX;
            my = event.touches[0].clientY;
            canvas.addEventListener( "mouseup", onMouseUp, false );
            canvas.addEventListener( "touchend", onTouchEnd, false );
            canvas.addEventListener( "mousemove", onMouseMove, false );
            canvas.addEventListener( "touchmove", onTouchMove, false );
            if(!tapped){ //if tap is not set, set up single tap
                tapped = setTimeout(function() {
                    tapped = null
                }, 300);   //wait 300ms then run single click code
            } else {    //tapped within 300ms of last tap. double tap
              clearTimeout(tapped); //stop single tap callback
              tapped = null;
              doubleClick();
            }
        }
    }

    function doubleClick() {
        toggleUI();
    }

    function onMouseUp(event) {
        if (leftButton(event)) {
            onTouchEnd(event);
        }
    }

    function onTouchEnd(event) {
        canvas.removeEventListener( "mouseup", onMouseUp, false );
        canvas.removeEventListener( "touchend", onTouchEnd, false );
        canvas.removeEventListener( "mousemove", onMouseMove, false );
        canvas.removeEventListener( "touchmove", onTouchMove, false );
        clicked = !dragging;
        dragging = false;
    }

    function onMouseMove(event) {
        event.touches = [{clientX: event.clientX, clientY: event.clientY}];
        onTouchMove(event);
    }

    // mouse drag -> move camera (adjusted to zoom)
    function onTouchMove(event) {
        // dont handle multi touch
        if (event.touches.length === 1) {
            dragging = true;
            event.preventDefault();
            var deltaX = event.touches[0].clientX - mx;
            var deltaY = event.touches[0].clientY - my;

            scalaObj.scene.rotateView( deltaX, deltaY );

            mx = event.touches[0].clientX;
            my = event.touches[0].clientY;
            // no need to update cam, projection matrix is not changed by translation
        }
    }

    var renderer = scalaObj.scene.renderer;
    var canvas = renderer.domElement;

    canvas.addEventListener( "mousedown", onMouseDown, false );
    canvas.addEventListener( "touchstart", onTouchStart, false );

    canvas.addEventListener( "dblclick", doubleClick, false );

    THREEx.WindowResize(renderer, updateProjection);
    THREEx.FullScreen.bindKey({ charCode : 'f'.charCodeAt(0) });

    // A cross-browser requestAnimationFrame
    // See https://hacks.mozilla.org/2011/08/animating-with-javascript-from-setinterval-to-requestanimationframe/
    var requestAnimFrame = (function() {
        return window.requestAnimationFrame    ||
            window.webkitRequestAnimationFrame ||
            window.mozRequestAnimationFrame    ||
            window.oRequestAnimationFrame      ||
            window.msRequestAnimationFrame     ||
            function(callback){
                window.setTimeout(callback, 1000 / 60);
            };
    })();

    // Don't run the game when the tab isn't visible
    window.addEventListener('focus', function() {
        unpause();
    });

    window.addEventListener('blur', function() {
        pause();
    });

//    var then = Date.now();
    var running = true;

    // Pause and unpause
    function pause() {
        running = false;
    }

    function unpause() {
        running = true;
//        then = Date.now();
        main();
    }

    // canvas & webgl context code

    updateProjection(window.innerWidth, window.innerHeight);
    renderer.setSize( window.innerWidth, window.innerHeight );
    mainContainer.appendChild( canvas );

    var stats = new Stats();
    stats.setMode( 1 ); // 0: fps, 1: ms, 2: mb

    stats.domElement.style.position = 'absolute';
    stats.domElement.style.left = '0px';
    stats.domElement.style.bottom = '0px';

    document.body.appendChild( stats.domElement );

    scalaObj.loadModel(toggleUI, requestAnimFrame, main);

    // The main game loop
    function main() {
        if(!running) {
            return;
        }

//        var now = Date.now();
//        var dt = now - then;

        stats.begin();

        if (takeScreenshot) {
            scalaObj.scene.renderNoTexture();
            var data = canvas.toDataURL("image/png").replace(/^data:image\/[^;]/, 'data:application/octet-stream');
            var evt = document.createEvent('MouseEvents');
            evt.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
            $("<a href='"+data+"' download='screenshot.png'/>")[0].dispatchEvent(evt);
            takeScreenshot = false;
        } else if (exportToMegaText) {
            var oldWidth = canvas.width;
            var oldHeight = canvas.height;
            var gl = renderer.getContext();
            var maxSize = gl.getParameter( gl.MAX_TEXTURE_SIZE );
            renderer.setSize( maxSize, maxSize );

            scalaObj.scene.renderNoTexture();

            var data = canvas.toDataURL("image/png").replace(/^data:image\/[^;]/, 'data:application/octet-stream');
            var evt = document.createEvent('MouseEvents');
            evt.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
            $("<a href='"+data+"' download='megatexture.png'/>")[0].dispatchEvent(evt);
            exportToMegaText = false;

            renderer.setSize( oldWidth, oldHeight );
        }

        // normal render
        scalaObj.scene.render();

        stats.end();

//        then = now;
        requestAnimFrame(main);
    }


};

window.onload = appMain;
