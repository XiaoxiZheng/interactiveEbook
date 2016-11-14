
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

  void track() {
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

  PVector getLerpedPos() {
    return lerpedLoc;
  }

  PVector getPos() {
    return loc;
  }

  void display() {
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

  int getThreshold() {
    return threshold;
  }

  void setThreshold(int t) {
    threshold =  t;
  }
}