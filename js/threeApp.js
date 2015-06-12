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

function arrToMat( arr ) {
    var mat = new THREE.Matrix4();
    mat.set( arr[0][0], arr[0][1], arr[0][2], arr[0][3],
             arr[1][0], arr[1][1], arr[1][2], arr[1][3],
             arr[2][0], arr[2][1], arr[2][2], arr[2][3],
             arr[3][0], arr[3][1], arr[3][2], arr[3][3] );
    return mat;
}

function appMain() {

    var scalaObj = demo.webapp.TutoMain();

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
    $("#centerView").click(centerView);
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
                var dockId = parseInt(evt.currentTarget.id.substring(3));
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
            $("#centerView").click(centerView(selectedVoxel));
        } else {
            $( ".leftMenu" ).hide();
        }
    }

    var translationMatrix = new THREE.Matrix4();
    function centerView(voxelId) {
        return function() {
            translationMatrix = arrToMat( scalaObj.translateToVoxel( voxelId ) );
            updateMVPs();
        };
    }

//    var cuts = getURLParameter("cells") || 0;
//    console.log( "cells:\t", cuts );
//    var cutPositions = new Array(2*cuts);
//    for ( var i = 0; i < cuts; i++ ) {
//        var cut = rndSpherePosition();
//        cutPositions[2*i] = cut[0];
//        cutPositions[2*i+1] = cut[1];
//    }

//    var id = getURLParameter( "polyId" ) || 3;
//    console.log( "polyId:\t", id );
//    var overlapThreshold = getURLParameter( "overlapThreshold" ) || 100;
//    console.log( "overlapThreshold:\t", overlapThreshold );

    var zoomMax = 16;
    var zoomMin = 0.0625;
    var zoomSpeed = 1.01;
    var zoom = 1;

    var camTheta = Math.PI / 2;
    var camPhi = Math.PI / 2;
    var camDist = 1;

    var projMat = new THREE.Matrix4();
    var viewMat = arrToMat( scalaObj.viewMatOf( camTheta, camPhi, camDist ) );
    var modelMat = new THREE.Matrix4();
    modelMat.elements[0] = Math.sqrt(2) / 2;
    modelMat.elements[2] = Math.sqrt(2) / 2;
    modelMat.elements[5] = 1;
    modelMat.elements[8] = -Math.sqrt(2) / 2;
    modelMat.elements[10] = Math.sqrt(2) / 2;
    var scaledModelMat = modelMat.clone();

    var mvp = new THREE.Matrix4();
    function updateMVPs() {
        var p = projMat.clone();
        var vp = p.multiply( viewMat );
        scaledModelMat = new THREE.Matrix4();
        scaledModelMat.multiplyScalar( zoom ).multiply( modelMat ).multiply( translationMatrix );
        scaledModelMat.elements[15] = 1;
        mvp = vp.multiply( scaledModelMat );
    }

    var updateProjection = function(screenWidth, screenHeight) {
        projMat = arrToMat( scalaObj.orthoMatrixFromScreen( screenWidth, screenHeight, 1.75 ) );
        updateMVPs();
    };

    updateProjection(window.innerWidth, window.innerHeight);

    function loadStdVoxel( id ) {
        scalaObj.loadVoxel( id );
        centerView(voxelId)();
        document.title = scalaObj.getVoxelName( id );
    }

    var mainContainer = document.getElementById( 'main' );

    // pinch detection (and more)
    var mc = new Hammer(mainContainer);
    mc.get("pinch").set({ enable: true });

    mc.on("pinch", function(ev) {
        zoomFct(ev.scale < 1 ? -1 : 1, 1.04);
    });

    var pixels = new Uint8Array(4);

    var renderer = scalaObj.renderer;
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

            modelMat = arrToMat( scalaObj.naiveRotMat( deltaX * 0.002, deltaY * 0.002 ) ).multiply( modelMat );
            updateMVPs();

            mx = event.touches[0].clientX;
            my = event.touches[0].clientY;
            // no need to update cam, projection matrix is not changed by translation
        }
    }

    function zoomFct(delta, alpha) {
        zoom = Math.min( Math.max( zoom * ( Math.pow( alpha, delta ) ), zoomMin ), zoomMax );
        updateMVPs();
    }

    // mouse wheel -> zoom in / out
    function onMouseWheel(event) {
        zoomFct( Math.max( -1, Math.min( 1, ( event.wheelDelta || -event.detail ) ) ), 1.05 );
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
    var gl = scalaObj.renderer.getContext();

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
                scalaObj.pickRender();

                // selection
                gl.readPixels(mx, innerHeight-my, 1, 1, gl.RGBA, gl.UNSIGNED_BYTE, pixels);
                var highlighted = 256*256*pixels[0] + 256*pixels[1] + pixels[2];
                var selection = scalaObj.selectFace(highlighted);
                var options = scalaObj.showDockingOptions();
                loadDockingOptions(options, parseInt(selection.voxelId), selection.faceInfo);
            }
            clicked = false;
        }

        // normal render
        var meshes = scalaObj.currentMeshes();
        for (var i = 0; i < meshes.length; i++) {
            meshes[i][0].material.uniforms.u_mvpMat.value = mvp;
            meshes[i][1].material.uniforms.u_mvpMat.value = mvp;
        }
        scalaObj.render();

//        then = now;
        requestAnimFrame(main);
    }


};

window.onload = appMain;