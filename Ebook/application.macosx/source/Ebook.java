import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import org.openkinect.processing.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Ebook extends PApplet {

// Daniel Shiffman and Thomas Sanchez Lengeling
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan

// https://github.com/shiffman/OpenKinect-for-Processing
// http://shiffman.net/p5/kinect/



// The kinect stuff is happening in another class
KinectTracker tracker;

public void setup() {
  //640,520

  tracker = new KinectTracker(this);
}

public void draw() {
  background(0);

  // Run the tracking analysis
  tracker.track();
  // Show the image
  tracker.display();

  // Let's draw the raw location
  PVector v1 = tracker.getPos();
  fill(50, 100, 250, 200);
  noStroke();
  //ellipse(v1.x, v1.y, 20, 20);

  // Let's draw the "lerped" location
  PVector v2 = tracker.getLerpedPos();
  fill(255, 91, 13, 200);
  noStroke();
  //ellipse(v2.x, v2.y, 20, 20);

  // Display some info
  int t = tracker.getThreshold();
  fill(0);
  text("threshold: " + t + "    " +  "framerate: " + PApplet.parseInt(frameRate) + "    " +
    "UP increase threshold, DOWN decrease threshold", 10, 500);
}

// Adjust the threshold with key presses
public void keyPressed() {
  int t = tracker.getThreshold();
  if (key == CODED) {
    if (keyCode == UP) {
      t +=5;
      tracker.setThreshold(t);
    } else if (keyCode == DOWN) {
      t -=5;
      tracker.setThreshold(t);
    }
  }
}

// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan

// https://github.com/shiffman/OpenKinect-for-Processing
// http://shiffman.net/p5/kinect/

class KinectTracker {

  PImage fgImg;
  PImage bgImg;

  // Depth threshold
  int threshold = 745;

  // Raw location
  PVector loc;

  // Interpolated location
  PVector lerpedLoc;

  // Depth data
  int[] depth;

  // What we'll show the user
  PImage display;

  //Kinect2 class
  Kinect2 kinect2;

  KinectTracker(PApplet pa) {

    //enable Kinect2
    kinect2 = new Kinect2(pa);
    kinect2.initDepth();
    kinect2.initDevice();

    // Make a blank image
    display = createImage(kinect2.depthWidth, kinect2.depthHeight, RGB);
    // Set up the vectors
    loc = new PVector(0, 0);
    lerpedLoc = new PVector(0, 0);

    fgImg = loadImage("sunset.jpg");
    bgImg = loadImage("bg.jpg");

    float fgImgNewW = map(fgImg.width,0, fgImg.width,0,display.width);
    float fgImgNewH = map(fgImg.height,0, fgImg.height,0,display.height);
    fgImg.resize((int)fgImgNewW,(int)fgImgNewH);

    float bgImgNewW = map(bgImg.width,0, bgImg.width,0,display.width);
    float bgImgNewH = map(bgImg.height,0, bgImg.height,0,display.height);
    fgImg.resize((int)bgImgNewW,(int)bgImgNewH);

    //pixel info
    fgImg.loadPixels();
    bgImg.loadPixels();
  }

  public void track() {
    // Get the raw depth as array of integers
    depth = kinect2.getRawDepth();

    // Being overly cautious here
    if (depth == null) return;

    float sumX = 0;
    float sumY = 0;
    float count = 0;

    for (int x = 0; x < kinect2.depthWidth; x++) {
      for (int y = 0; y < kinect2.depthHeight; y++) {
        // Mirroring the image
        int offset = kinect2.depthWidth - x - 1 + y * kinect2.depthWidth;
        // Grabbing the raw depth
        int rawDepth = depth[offset];

        // Testing against threshold
        if (rawDepth > 0 && rawDepth < threshold) {
          sumX += x;
          sumY += y;
          count++;
        }
      }
    }
    // As long as we found something
    if (count != 0) {
      loc = new PVector(sumX/count, sumY/count);
    }

    // Interpolating the location, doing it arbitrarily for now
    lerpedLoc.x = PApplet.lerp(lerpedLoc.x, loc.x, 0.3f);
    lerpedLoc.y = PApplet.lerp(lerpedLoc.y, loc.y, 0.3f);
  }

  public PVector getLerpedPos() {
    return lerpedLoc;
  }

  public PVector getPos() {
    return loc;
  }

  public void display() {
    PImage img = kinect2.getDepthImage();

    // Being overly cautious here
    if (depth == null || img == null) return;

    // Going to rewrite the depth image to show which pixels are in threshold
    // A lot of this is redundant, but this is just for demonstration purposes
    display.loadPixels();
    for (int x = 0; x < kinect2.depthWidth; x++) {
      for (int y = 0; y < kinect2.depthHeight; y++) {
        // mirroring image
        int offset = (kinect2.depthWidth - x - 1) + y * kinect2.depthWidth;
        // Raw depth
        int rawDepth = depth[offset];
        int pix = x + y*display.width;
        if (rawDepth > 0 && rawDepth < threshold) {
          // A red color instead
          display.pixels[pix] = bgImg.pixels[(bgImg.width - x - 1) + y * bgImg.width];//color(127,107,101);//fgImg.pixels[x*y];//color(150, 50, 50);
        } else {
          display.pixels[pix] = color(0);//bgImg.pixels[(bgImg.width - x - 1) + y * bgImg.width];//img.pixels[offset];
        }
      }
    }
    display.updatePixels();

    // Draw the image
    image(display, 0, 0);
  }

  public int getThreshold() {
    return threshold;
  }

  public void setThreshold(int t) {
    threshold =  t;
  }
}
  public void settings() {  size(640, 520); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "Ebook" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
