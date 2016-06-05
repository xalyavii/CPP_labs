package Ant;

import java.util.Arrays;

/*
  * Visualization of a mathematical model of ant Langton
  * refutation of theory of All
  * small ant (a cell) run throw the field
  * if it step on white cell, it go to the right
  * if it step on black cell, it go to the left
  * when ant go away from cell, cell change color
  * */

public class Model {
  private byte[] field = null; //array with field
  private int width, height, x, y, course = 0;
  /*
  * width and height of field
  * x, y - coordinates of the ant
  * course - where the ant have to go:
  * 0 - up
  * 1 - right
  * 2 - down
  * 3 - left
  * */

  //initialization
  public Model(int width, int height) {
    this.width = width;
    this.height = height;
    this.x = width / 2;
    this.y = height / 2;
    field = new byte[width * height];
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getCourse() {
    return course;
  }

  public void setX(int a) {
    x = a;
  }

  public void setY(int a) {
    y = a;
  }

  public void setCourse(int a) {
    course = a;
  }

  public void clear() {
    Arrays.fill(field, (byte) 0);
  }


  public void setCell(int x, int y, byte c) {
    field[y * width + x] = c;
  }

  public byte getCell(int x, int y) {
    return field[y * width + x];
  }


  public void setField(byte[] temp) {
    field = temp;
  }

  public byte[] getField() {
    return field;
  }


  public void randomByte() {
    for (int i = 0; i < width * height; i++) {
      field[i] = (byte) (0 + (int) (Math.random() * ((1 - 0) + 1)));
    }
  }


  public void simulate() { //one step of the ant
    /*
    * in the begin we go step in necessary course
    * all our field is a torus. so we should provide passage through the border
    * */
    switch (course) {
      case 0: {
        y = (y + 1) % this.height;
        break;
      }
      case 1: {
        x = (x + 1) % this.width;
        break;
      }
      case 2: {
        y = (y + height - 1) % this.height;
        break;
      }
      case 3: {
        x = (x + width - 1) % this.width;
        break;
      }
    }

    byte temp = this.getCell(this.x, this.y); //get color of cell
    // change course according the color
    if (temp == (byte) 1) {
      course -= 1;
      if (course < 0) {
        course = 3;
      }
      this.setCell(x, y, (byte) 0);
    } else {
      course += 1;
      course = course % 4;
      this.setCell(x, y, (byte) 1);
    }
  }
}
