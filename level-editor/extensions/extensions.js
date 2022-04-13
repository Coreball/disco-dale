/// <reference types="@mapeditor/tiled-api" />

var COLOR_NO_MOVEMENT = 0;
var COLOR_SCROLL_H = 1;
var COLOR_SCROLL_V = 2;
var COLOR_ROTATE = 3;

var circleTool = tiled.registerTool("ColorWheel", {
    name: "Create Rotating Colors",

    mousePressed: function(button, x, y, modifiers) {
        var objectLayer = this.map.currentLayer;
        if (objectLayer && objectLayer.isObjectLayer) {
            this.pressed = true;
            this.x = x;
            this.y = y;
            try {
                if (this.object !== null && this.object !== undefined) objectLayer.removeObject(this.object);
                if (this.point !== null && this.point !== undefined) objectLayer.removeObject(this.point);
            } catch (e) {}
            this.object = new MapObject("colorwheel");
            this.object.shape = MapObject.Ellipse;
            this.object.x = this.x;
            this.object.y = this.y;
            this.height = 1;
            this.width = 1;
            this.point = new MapObject();
            this.point.x = this.x;
            this.point.y = this.y;
            this.point.shape = MapObject.Point;
            objectLayer.addObject(this.object);
            // objectLayer.addObject(this.point);
        }
    },

    mouseMoved: function(x, y, modifiers) {
        if (!this.pressed) return;
        var objectLayer = this.map.currentLayer;
        // if (this.object !== null && this.object !== undefined) objectLayer.removeObject(this.object);
        var r = Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
        var a = Math.atan2(y - this.y, x - this.x);
        this.object.height = 2 * r;
        this.object.width = 2 * r;
        this.object.x = this.x - r;
        this.object.y = this.y - r;
    },

    mouseReleased: function(button, x, y, modifiers) {
        this.pressed = false;
        this.map.setProperty("colorMode", COLOR_ROTATE);
    }
});

var nAction = tiled.registerAction("ColorStill", function(action) {
    if (tiled.activeAsset.isTileMap) {
        tiled.activeAsset.setProperty("colorMode", COLOR_NO_MOVEMENT);
        var layer = tiled.activeAsset.currentLayer;
        var colorwheel = layer.objects.find(e => e.name == "colorwheel");
        try { layer.removeObject(colorwheel); } catch (e) {}
    }
});
nAction.text = "Set Color No Movement";
nAction.checkable = false;

var hAction = tiled.registerAction("ColorHorizontal", function(action) {
    if (tiled.activeAsset.isTileMap) {
        tiled.activeAsset.setProperty("colorMode", COLOR_SCROLL_H);
        var layer = tiled.activeAsset.currentLayer;
        var colorwheel = layer.objects.find(e => e.name == "colorwheel");
        try { layer.removeObject(colorwheel); } catch (e) {}
    }
});
hAction.text = "Set Color Scroll Horizontal";
hAction.checkable = false;

var vAction = tiled.registerAction("ColorVertical", function(action) {
    if (tiled.activeAsset.isTileMap) {
        tiled.activeAsset.setProperty("colorMode", COLOR_SCROLL_V);
        var layer = tiled.activeAsset.currentLayer;
        var colorwheel = layer.objects.find(e => e.name == "colorwheel");
        try { layer.removeObject(colorwheel); } catch (e) {}
    }
});
vAction.text = "Set Color Scroll Vertical";
vAction.checkable = false;

tiled.extendMenu("Map", [
    { action: "ColorStill" },
    { action: "ColorHorizontal" },
    { action: "ColorVertical" }
]);