/*
 lissapong
 Let the epic crt battle over lissajous begin!
 mouseY, move user paddle
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

boolean autoPlay = true, gameover = true, initScore = false, demoMode = true;
PVector c, p;
float cx = 1, cy = 2, s = 30, f = 10.5, paddleW = 10, paddleH = 150;
int gameTimer = 0, score1 = 0, score2 = 0, winner = 0;

void setup() {
  size(512, 512);

  xy = new XYscope(this);
  xy.rectMode(CENTER);
  
  pongReset();
}

void draw() {
  background(0);
  xy.clearWaves();

  // render scores
  float s1 = 25;
  float s2 = 25;
  if (winner == -1) {
    s1 += sin(frameCount*.5)*20;
  } else if (winner == 1) {
    s2 += sin(frameCount*.5)*20;
  }

  xy.textSize(s1);
  xy.text(score1, 50, 50);
  xy.textSize(s2);
  xy.text(score2, width-50, 50);

  // gameplay vs gameover
  if (!gameover) {
    pongUpdate();
    xy.lissajous(c.x, c.y, s, cx, cy, frameCount, 60);

    float ly = c.y + (noise(frameCount*.023)*height-c.y)*c.x/width;
    xy.rect(paddleW/2, ly, paddleW, paddleH);

    float ry = c.y + (noise(frameCount*.02)*height-c.y)*(1 - c.x/width);
    if (!autoPlay) {
      ry = p.y;
    }
    xy.rect(width-paddleW/2, ry, paddleW, paddleH);

    if (frameCount%30==0 && autoPlay) {
      demoMode = !demoMode;
    }
    if (demoMode && autoPlay) {
      xy.textSize(15);
      xy.textAlign(CENTER, CENTER);
      xy.text("CLICK TO PLAY", width/2, 50);
    }
  } else {
    pushMatrix();
    xy.textSize(40 + sin(frameCount*.05)*20);
    xy.textAlign(CENTER, CENTER);
    translate(width/2, height/2);
    rotate(radians(cos(frameCount*.01)*15));
    xy.text("GAME OVER", 0, 0);
    popMatrix();
  }

  xy.buildWaves();

  xy.drawXY();
  xy.drawWaveform();
}

void mousePressed() {
  if (gameover) {
    pongReset();
  }

  if (autoPlay) {
    autoPlay = false;
    pongReset();
  }
}

void pongReset() {
  gameover = false;
  c = new PVector(width/2, height/2);
  p = new PVector(0, c.y);
  cx = 1;
  cy = 2;
  xy.freq(cx * cy * 10);
  f = 10.5;
  gameTimer = 0;
  score1 = 0;
  score2 = 0;
  winner = 0;
  paddleH = 150;
  demoMode = true;
}

void pongUpdate() {
  p.y = mouseY;
  c.x += cx;
  c.y += cy;

  if (!autoPlay && (abs(c.y-p.y) > (paddleH/2 + s) && c.x >= (width-paddleW/2-s)) ) {
    println(abs(c.y - p.y) + " / "+  (paddleH/2 + s));
    gameover = true;
    if (score1 >= score2) {
      winner = -1;
    } else {
      winner = 1;
    }
    xy.freq(random(4, 20));
    xy.resetWaves();
  }

  if ((c.x > (width-paddleW/2-s) || c.x < (s-paddleW))) {
    changeX();
    changeY();
    xy.freq(cx*cy);
    xy.resetWaves();
    cx *= -1;
    initScore = true;
    if (paddleH > paddleW) {
      paddleH--;
    }
  }

  if (c.y > height-s/2 || c.y < s/2) {
    cy *= -1;
  }
  if (cy == height-s/2) {
    cy -= s/2;
  } else if (cy == s/2) {
    cy += s/2;
  }


  gameTimer++;
}

void changeX() {
  float fx = ceil(random(1, 10));
  if (c.x < s)
    fx *=-1;

  if (fx > 0) {
    score2++;
  } else {
    score1++;
  }
  cx = fx;
}

void changeY() {
  float fy = ceil(random(1, 10));
  if (!autoPlay && c.x > width/2) {
    fy = abs(c.y - p.y)/10;
  }
  if (c.y > height-s/2) {
    fy *=-1;
  }
  cy = fy;
}
