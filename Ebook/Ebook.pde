import org.openkinect.processing.*;
//http://shiffman.net/p5/kinect/

Kinect2 kinect2;

void setup() {
  size(800,600);
  kinect2 = new Kinect2(this);
  kinect2.initDepth();
  kinect2.initDevice();
}

void draw(){
  background(0);
  
  PImage img = kinect2.getDepthImage();
  image(img,0,0);
}