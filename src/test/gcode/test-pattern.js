// Test pattern 

if (!doc.HasLayer("captions")) {
	doc.CreateLayer("captions");
}
doc.ActiveLayerName = "Default";
var squaresLayer = doc.ActiveLayer;

doc.ActiveLayerName = "captions";
var captionsLayer = doc.ActiveLayer;


squaresLayer.Entities.Clear();
captionsLayer.Entities.Clear();

doc.MachineOps.Clear();
doc.MachiningOptions.OutFile = "a.nc";

var cellW = .260;
var cellH = .255;
var w = .25;
var h = w;

var x = 0;
var y = 0;
var z = 0;
var depth = -.002;


var points = generatePoints();
shufflePoints(points);
drawStoredPoints(points);
addProfile();
addEngrave();

function addProfile() {
	var mop = new MOPProfile(doc, squaresLayer.Entities);
	mop.InsideOutside = "Inside";
	mop.TargetDepth = -.002;
	mop.CutFeedrate = 12;
	mop.PlungeFeedrate = 8;
	mop.ToolDiameter = .016;
    mop.ToolNumber = 0;
    mop.ClearancePlane = 0.1;
    doc.MachineOps.Add(mop);
}

function addEngrave() {
	var mop = new MOPEngrave(doc, captionsLayer.Entities);

	mop.TargetDepth = -.002;
	mop.CutFeedrate = 12;
	mop.PlungeFeedrate = 8;
	mop.ToolDiameter = .016;
    mop.ToolNumber = 0;
    mop.ClearancePlane = 0.1;
    doc.MachineOps.Add(mop);
}

function shufflePoints(points) {
	fisherYates(points);
}

function fisherYates ( myArray ) {
  var i = myArray.length;
  if ( i == 0 ) return false;
  while ( --i ) {
     var j = Math.floor( Math.random() * ( i + 1 ) );
     var tempi = myArray[i];
     var tempj = myArray[j];
     myArray[i] = tempj;
     myArray[j] = tempi;
   }
}

function generatePoints() {
	var points = [];
	for (var i = 0; i < 4; i++) {
		for (var j = 0; j < 10; j++) {
			points.push([i*cellW,j*cellH]);
		}
		var separation = int((cellH - h) * 1000);
	    drawColumnCaption(separation , i * cellW + .05);
		cellH += .005;
	
	}
	return points;
}

function drawStoredPoints(points) {
	for (var k in points) {
		x = points[k][0];
		y = points[k][1];
		rect(x,y)
	}
}	
function drawColumnCaption(c, x) {
	var mt = new MText();
	mt.Text = c+"";
	mt.Height = ".15";
	mt.Location = x + ",0,-.002";
	captionsLayer.Entities.Add(mt);
}

function rect(x,y) {
	var p = new Polyline();
	p.Add(x,y,depth);
	p.Add(x+w,y,depth);
	p.Add(x+w,y+h,depth);
	p.Add(x,y+h,depth);
	p.Add(x,y,depth);
	p.Closed = true;
	squaresLayer.Entities.Add(p);
}
