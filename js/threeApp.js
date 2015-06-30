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

    scalaObj.main();

    var seed = getURLParameter("seed") || Math.random().toString().slice(2);
    console.log("seed:\t", seed);

    // help dialog
    $(function() {
        $( "#dialog" ).dialog({
            width: 600,
            height: 222
        });
    });
    $( "#dialog" ).dialog( "close" );

    function toggleUI() {
        console.log("toggling ui");
        if ( $( "#gui" ).css( "display" ) === "none" )
            $( "#gui" ).show();
        else
            $( "#gui" ).hide();
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
    $("#axis").click(function axisFct() {
            scalaObj.scene.toggleAxis();
        }
    );
    $("#axis").attr("checked", false);
    $("#axis").prop("checked", false);

    $("#borders").unbind("click");
    $("#borders").click(function bordersFct() {
            scalaObj.scene.toggleBorders();
        }
    );
    $("#borders").attr("checked", true);
    $("#borders").prop("checked", true);

    $("#rndColors").unbind("click");
    $("#rndColors").click(function rndColorsFct() {
            scalaObj.toggleRndColors();
            loadColors(scalaObj.selectFace(highlighted));
        }
    );
    var rndCols = scalaObj.colorsAreRandom();
    $("#rndColors").attr("checked", rndCols);
    $("#rndColors").prop("checked", rndCols);

    var takeScreenshot = false;
    var tmpImg = null;
    function screenshot() {
        takeScreenshot = true;
        var w = window.open('', '');
        w.document.title = "Screenshot";
        tmpImg = new Image();
        w.document.body.appendChild(tmpImg);
        // force frame rendering to get screenshot data
        unpause();
        pause();
        tmpImg = null;
    }
    $("#screenshot").unbind("click");
    $("#screenshot").click(screenshot);

    var toggleFullscreen = THREEx.FullScreen.toggleFct();
    $("#fullscreen").unbind("click");
    $("#fullscreen").click(toggleFullscreen);

    $( ".leftMenu" ).hide();

    function showFaceMenu( selectedFace ) {
        if ( selectedFace > -1 ) {
            $( ".leftMenu" ).show();
        } else {
            $( ".leftMenu" ).hide();
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

    var updateProjection = function(screenWidth, screenHeight) {
        scalaObj.scene.updateViewport( screenWidth, screenHeight );
    };

    updateProjection(window.innerWidth, window.innerHeight);

    var mainContainer = document.getElementById( 'main' );

    // pinch detection (and more)
    var mc = new Hammer(mainContainer);
    mc.get("pinch").set({ enable: true });

    mc.on("pinch", function(ev) {
        scalaObj.scene.zoom(ev.scale < 1 ? -1 : 1);
    });

    var pixels = new Uint8Array(4);

    var renderer = scalaObj.scene.renderer;
    var canvas = renderer.domElement;
    renderer.setSize( window.innerWidth, window.innerHeight );
    mainContainer.appendChild( canvas );

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
    main();


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

    // The main game loop
    function main() {
        if(!running) {
            return;
        }

//        var now = Date.now();
//        var dt = now - then;

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
            tmpImg.src = renderer.domElement.toDataURL("image/png");
            takeScreenshot = false;
        }

//        then = now;
        requestAnimFrame(main);
    }


};

window.onload = appMain;