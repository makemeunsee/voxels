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

    demo.webapp.Shaders().loadShaders(document.getElementById('shader-vs').innerHTML,
                                      document.getElementById('shader-fs').innerHTML,
                                      document.getElementById('shader-pick-vs').innerHTML,
                                      document.getElementById('shader-pick-fs').innerHTML,
                                      document.getElementById('shader-axis-vs').innerHTML,
                                      document.getElementById('shader-axis-fs').innerHTML);

    var cuts = getURLParameter("cuts") || 2000;
    console.log("cuts:\t", cuts);

    var seed = getURLParameter("seed") || Math.random().toString().slice(2);
    console.log("seed:\t", seed);

    scalaObj.initRnd(seed);

    // help dialog
    $(function() {
        $( "#dialog" ).dialog({
            autoOpen: false,
            width: 600,
            height: 222
        });
    });

    var uiVisible = true;
    function toggleUI() {
        console.log("toggling ui");
        uiVisible = !uiVisible;
        if ( uiVisible ) {
            $( ".gui" ).show();
            stats.domElement.style.display = "block";
        } else {
            $( ".gui" ).hide();
            stats.domElement.style.display = "none";
        }
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

    $("#axis").unbind("click");
    $("#axis").click(function() {
            scalaObj.scene.toggleAxis();
        }
    );
    $("#axis").attr("checked", false);
    $("#axis").prop("checked", false);

    $("#rndColors").unbind("click");
    $("#rndColors").click(function() {
            scalaObj.toggleRndColors();
            loadColors(scalaObj.selectFace(highlighted));
        }
    );
    $("#rndColors").attr("checked", true);
    $("#rndColors").prop("checked", true);

    $("#cullback").unbind("click");
    $("#cullback").click(function() {
            scalaObj.scene.toggleCullback();
        }
    );
    $("#cullback").attr("checked", false);
    $("#cullback").prop("checked", false);

    $( "#downsamplingSlider" ).slider( {
        orientation: "horizontal",
        min: 0,
        max: 7,
        value: 0,
        slide: refreshDownsampling,
        change: refreshDownsampling
    });

    function refreshDownsampling(evt, ui) {
        scalaObj.scene.setDownsampling(ui.value);
    }

    $( "#bordersSlider" ).slider( {
        orientation: "horizontal",
        min: 0,
        max: 20,
        value: 10,
        slide: refreshBordersWidth,
        change: refreshBordersWidth
    });

    function refreshBordersWidth(evt, ui) {
        scalaObj.scene.setBordersWidth(ui.value);
    }

    $( "#explosionSlider" ).slider( {
        orientation: "horizontal",
        min: 0,
        max: 100,
        value: 0,
        slide: refreshExplosion,
        change: refreshExplosion
    });

    function refreshExplosion(evt, ui) {
        scalaObj.scene.setExplosion(ui.value);
    }

    $( "#depthSlider" ).slider( {
        orientation: "horizontal",
        min: -100,
        max: 100,
        value: 50,
        slide: refreshDepth,
        change: refreshDepth
    });

    function refreshDepth(evt, ui) {
        scalaObj.scene.setDepthScale(ui.value);
    }

    $( ".ui-slider-handle" ).css( { "width": "0.5em" } );

    var takeScreenshot = false;
    function screenshot() {
        takeScreenshot = true;
    }
    $("#screenshot").unbind("click");
    $("#screenshot").click(screenshot);

    var toggleFullscreen = THREEx.FullScreen.toggleFct();
    $("#fullscreen").unbind("click");
    $("#fullscreen").click(toggleFullscreen);

    $( "#rightColumn" ).hide();

    function showFaceMenu( selectedFace ) {
        if ( selectedFace > -1 && uiVisible) {
            $( "#rightColumn" ).show();
        } else {
            $( "#rightColumn" ).hide();
        }
    }

    $("#color").change(function() {
        scalaObj.changeFaceColor($("#color").prop("value"));
    });
    $("#centerColor").change(function() {
        scalaObj.changeFaceCenterColor($("#centerColor").prop("value"));
    });

    function loadColors(selection) {
        var color = selection.faceColor;
        var centerColor = selection.faceCenterColor;
        if (color !== -1) {
            $("#color").attr("value", "#"+color);
            $("#color").prop("value", "#"+color);
        }
        if (centerColor !== -1) {
            $("#centerColor").attr("value", "#"+centerColor);
            $("#centerColor").prop("value", "#"+centerColor);
        }
    }

    var mainContainer = document.getElementById( 'main' );

    // pinch detection (and more)
    var mc = new Hammer(mainContainer);
    mc.get("pinch").set({ enable: true });

    mc.on("pinch", function(ev) {
        scalaObj.scene.zoom(ev.scale < 1 ? -1 : 1);
    });

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

    function clearSelection() {
        scalaObj.clearSelection();
        loadDockingOptions( 0 );
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

    // mouse wheel -> zoom in / out
    function onMouseWheel(event) {
        scalaObj.scene.zoom( Math.max( -1, Math.min( 1, ( event.wheelDelta || -event.detail ) ) ) );
    }

    var renderer = scalaObj.scene.renderer;
    var canvas = renderer.domElement;

    canvas.addEventListener( "mousedown", onMouseDown, false );
    canvas.addEventListener( "touchstart", onTouchStart, false );

    canvas.addEventListener( "mousewheel", onMouseWheel, false );
      // Firefox
    canvas.addEventListener( "DOMMouseScroll", onMouseWheel, false );

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

    reset();
//    var then = Date.now();
    var running = true;


    // Functions ---

    // Reset game to original state
    function reset() {
    }

    // Pause and unpause
    function pause() {
        running = false;
    }

    function unpause() {
        running = true;
//        then = Date.now();
        main();
    }

    var highlighted = 0;

    // canvas & webgl context code

    updateProjection(window.innerWidth, window.innerHeight);

    var pixels = new Uint8Array(4);

    renderer.setSize( window.innerWidth, window.innerHeight );
    mainContainer.appendChild( canvas );

    var stats = new Stats();
    stats.setMode( 1 ); // 0: fps, 1: ms, 2: mb

    stats.domElement.style.position = 'absolute';
    stats.domElement.style.left = '0px';
    stats.domElement.style.bottom = '0px';

    document.body.appendChild( stats.domElement );

    scalaObj.loadModel(cuts);

    main();

    // The main game loop
    function main() {
        if(!running) {
            return;
        }

//        var now = Date.now();
//        var dt = now - then;

        stats.begin();

        if ( clicked ) {

            // pickRender
            highlighted = scalaObj.scene.pickRender(mx,my);
            var selection = scalaObj.selectFace(highlighted);
            loadColors(selection);
            showFaceMenu(selection.faceId);
            clicked = false;
        }

        // normal render
        scalaObj.scene.render(highlighted);

        if (takeScreenshot) {
            var data = renderer.domElement.toDataURL("image/png").replace(/^data:image\/[^;]/, 'data:application/octet-stream');
            var evt = document.createEvent('MouseEvents');
            evt.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
            $("<a href='"+data+"' download='screenshot.png'/>")[0].dispatchEvent(evt);
            takeScreenshot = false;
        }

        stats.end();

//        then = now;
        requestAnimFrame(main);
    }


};

window.onload = appMain;
