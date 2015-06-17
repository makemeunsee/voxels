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

function selectText(element) {
    var doc = document;
    var text = doc.getElementById(element);

    if (doc.body.createTextRange) { // ms
        var range = doc.body.createTextRange();
        range.moveToElementText(text);
        range.select();
    } else if (window.getSelection) { // moz, opera, webkit
        var selection = window.getSelection();
        var range = doc.createRange();
        range.selectNodeContents(text);
        selection.removeAllRanges();
        selection.addRange(range);
    }
}

function appMain() {

    var scalaObj = demo.webapp.VoxelMain();
    demo.webapp.Shaders().loadShaders(document.getElementById('shader-vs').innerHTML,
                                      document.getElementById('shader-fs').innerHTML,
                                      document.getElementById('shader-pick-vs').innerHTML,
                                      document.getElementById('shader-pick-fs').innerHTML);

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

    function save() {
        var buildCode = scalaObj.buildCode();
        var selfUrl = window.location.href.toString().split("?")[0];
        console.log(buildCode);
        var buildUrl = selfUrl+"?code="+buildCode
        $( "#clipboard" ).empty();
        $( "#clipboard" ).append("<a id='buildUrl' href='"+buildUrl+"'>"+buildUrl+"</a>");
        $( "#clipboard" ).dialog({
            width: 500,
            height: 150
        });
        selectText("buildUrl");
    }
    $("#save").unbind("click");
    $("#save").click(save);

    $("#reset").unbind("click");
    $("#reset").click(function() {
        window.location = window.location.pathname;
    });

    $("#borders").unbind("click");
    $("#borders").click(function bordersFct() {
            scalaObj.scene.toggleBorders();
        }
    );
    $("#borders").attr("checked", true);

    $("#rndColors").unbind("click");
    $("#rndColors").click(function rndColorsFct() {
            scalaObj.toggleRndColors();
        }
    );
    $("#rndColors").attr("checked", true);

    // TODO ensure checkboxes properly reset after reload

    function screenshot() {
        var w = window.open('', '');
        w.document.title = "Screenshot";
        w.document.body.style.backgroundColor = "black";
        var img = new Image();
        img.src = canvas.toDataURL("image/png");
        w.document.body.appendChild(img);
    }
    $("#screenshot").unbind("click");
    $("#screenshot").click(screenshot);

    var toggleFullscreen = THREEx.FullScreen.toggleFct();
    $("#fullscreen").unbind("click");
    $("#fullscreen").click(toggleFullscreen);

    $("#centerView").unbind("click");
    $("#centerView").button();

    $( ".leftMenu" ).hide();

    var voxelId = -1;
    var count = scalaObj.voxelTypeCount;

    (function () {
        var menu0 = $( "#menu0" );
        var menu1 = $( "#menu1" );
        menu0.empty();
        menu1.empty();
        menu0.append('<li id="li" class="ui-widget-header">Initial voxel</li>');
        menu1.append('<li id="li" class="ui-widget-header">&#xA0;</li>');
        for (var i = 0; i < count; i++) {
            var menu = i%2==0 ? menu0 : menu1;
            menu.append('<li id="li-'+i+'" class="ui-menu-item">'+scalaObj.getVoxelName(i)+'</li>');
        }
        $( "li.ui-menu-item" ).hover(
            function(evt) {
                $( this ).addClass( "active" );
                voxelId = evt.currentTarget.id.substring(3);
                loadStdVoxel( voxelId );
            },
            function(evt) {
                $( this ).removeClass( "active" );
                voxelId = -1;
                scalaObj.unloadVoxel();
            }
        );
        $( "li.ui-menu-item" ).click(
            function(evt) {
                menu0.empty();
                menu1.empty();
                menu0.hide();
                menu1.hide();
            }
        );
        menu0.show();
        menu1.show();
    })();

    function loadDockingOptions( options, selectedVoxel, faceInfo ) {
        var menu0 = $( "#menu0" );
        var menu1 = $( "#menu1" );
        menu0.empty();
        menu1.empty();
        var hasSome = false;
        menu0.append('<li id="li" class="ui-widget-header">Voxel: '+selectedVoxel+'</li>');
        menu1.append('<li id="li" class="ui-widget-header">&#xA0</li>');
        var i = 0;
        for (var prop in options) {
            if (options.hasOwnProperty(prop)) {
                hasSome = true;
                var menu = i%2==0 ? menu0 : menu1;
                menu.append('<li id="li-'+prop+'" class="ui-menu-item">'+options[prop]+'</li>');
                i++;
            }
        }

        $( "li.ui-menu-item" ).hover(
            function(evt) {
                $( this ).addClass( "active" );
                var dockId = parseInt(evt.currentTarget.id.substring(3));
                scalaObj.dockVoxel(dockId);
            },
            function(evt) {
                $( this ).removeClass( "active" );
                scalaObj.undockLastVoxel();
            }
        );
        $( "li.ui-menu-item" ).click(
            function(evt) {
                // docking actually happened during hover
                clearSelection();
            }
        );

        if (hasSome) {
            $( ".leftMenu" ).show();
            $("#centerView").click(
                function() {
                    scalaObj.centerViewOn(selectedVoxel);
                }
            );
        } else {
            $( ".leftMenu" ).hide();
        }
    }

    var buildCode = getURLParameter("code") || "";
    console.log(buildCode);
    if (buildCode.length > 0) {
        var voxelId = scalaObj.loadCode(buildCode);
        if (voxelId > -1) {
            $( ".leftMenu" ).hide();
        }
    }

    var updateProjection = function(screenWidth, screenHeight) {
        scalaObj.scene.updateViewport( screenWidth, screenHeight );
    };

    updateProjection(window.innerWidth, window.innerHeight);

    function loadStdVoxel( id ) {
        scalaObj.loadVoxel( id );
        document.title = scalaObj.getVoxelName( id );
    }

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
//        toggleUI();
        scalaObj.deleteSelected();
    }

    function clearSelection() {
        scalaObj.clearSelection();
        if (voxelId != -1) {
            loadDockingOptions(null, -1, "");
        }
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

    var simTime = 0.0;
    var gl = renderer.getContext();

    var highlighted = 0;

    // The main game loop
    function main() {
        if(!running) {
            return;
        }

//        var now = Date.now();
//        var dt = now - then;

        if ( clicked ) {

            if ( voxelId != -1 ) {
                // pickRender
                scalaObj.scene.pickRender();

                // selection
                gl.readPixels(mx, innerHeight-my, 1, 1, gl.RGBA, gl.UNSIGNED_BYTE, pixels);
                highlighted = 256*256*pixels[0] + 256*pixels[1] + pixels[2];
                var selection = scalaObj.selectFace(highlighted);
                var options = scalaObj.showDockingOptions();
                loadDockingOptions(options, parseInt(selection.voxelId), selection.faceInfo);
            }
            clicked = false;
        }

        // normal render
        scalaObj.scene.render(highlighted);

//        then = now;
        requestAnimFrame(main);
    }


};

window.onload = appMain;