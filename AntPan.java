package Ant;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.*;

import javax.swing.JPanel;

/*
 * Editor panel
 * Left mouse button sets the black cells, right - white
 * Editing is available even during a running process.
 * Process of simulation going in another stream
 */
public class AntPan extends JPanel implements Runnable {

  private Thread simThread = null;
  private Model ant = null;
  //Delay between steps (ms)
  private int updateDelay = 100;
  //Size of a cell
  private int cellSize = 8;
  //Interval between cells
  private int cellInterval = 1;
  //Colors
  private static final Color c0 = new Color(0xffffff);
  private static final Color c1 = new Color(0x000000);

  //Creating of variables and files for saving
  private int saveByte = 0;
  private int readByte = 0;
  private int replayByte = 0;
  private File saveFile;
  private File loadFile;
  private File replayFile = new File("replay");
  private boolean endLoadFile = false;

  public AntPan() {
    setBackground(Color.BLACK);

    //field's editor

    //creating class for getting mouse events
    MouseAdapter mouseAdapter = new MouseAdapter() {
      private boolean pressedLeft = false;    //left mouse button pressed
      private boolean pressedRight = false;    //right mouse button pressed

      //Processor coordinate cursor position
      @Override
      public void mouseDragged(MouseEvent e) {
        setCell(e);
      }

      //Processor pressing
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          pressedLeft = true;
          pressedRight = false;
          setCell(e);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
          pressedLeft = false;
          pressedRight = true;
          setCell(e);
        }
      }

      //Releasing the button
      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          pressedLeft = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
          pressedRight = false;
        }
      }

      //Set/clear the cell.
      private void setCell(MouseEvent e) {
        if (ant != null) {
          synchronized (ant) {
            //coordinates of the cell count, which indicates the mouse
            int x = e.getX() / (cellSize + cellInterval);
            int y = e.getY() / (cellSize + cellInterval);
            if (x >= 0 && y >= 0 && x < ant.getWidth() && y < ant.getHeight()) {
              if (pressedLeft == true) {
                ant.setCell(x, y, (byte) 1);
                repaint();
              }
              if (pressedRight == true) {
                ant.setCell(x, y, (byte) 0);
                repaint();
              }
            }
          }
        }
      }
    };
    addMouseListener(mouseAdapter);
    addMouseMotionListener(mouseAdapter);
  }

  //function to access objects and variables outside the class
  public Model getAntModel() {
    return ant;
  }

  public void initialize(int width, int height) {
    ant = new Model(width, height);
  }

  public void setSaveByte(int g) {
    saveByte = g;
  }

  public void setLoadByte(int g) {
    readByte = g;
  }

  public void setReplayByte(int g) {
    replayByte = g;
  }

  public void setSaveFile(File f) {
    saveFile = f;
  }

  public void setLoadFile(File f) {
    loadFile = f;
  }

  public void replay() {
    String hight, widht, tempCellSize;
    byte[] tempField;

    try {
      BufferedReader in = new BufferedReader(new FileReader(replayFile.getAbsoluteFile()));
      try {
        char s[];
        String s1;

        while (true) {
          try {
            Thread.sleep(updateDelay);
          } catch (InterruptedException e) {
          }

          if (in.readLine() == null) break;

          widht = in.readLine();
          hight = in.readLine();
          tempCellSize = in.readLine();
          ant.setX(Integer.parseInt(in.readLine()));
          ant.setY(Integer.parseInt(in.readLine()));
          ant.setCourse(Integer.parseInt(in.readLine()));
          tempField = new byte[Integer.parseInt(widht) * Integer.parseInt(hight)];
          int k = 0;

          for (int i = 0; i < Integer.parseInt(hight); i++) {
            s1 = in.readLine();
            s = s1.toCharArray();
            for (int g = 0; g < Integer.parseInt(widht); g++) {
              if (s[g] == '1') {
                tempField[k] = 1;
              } else {
                tempField[k] = 0;
              }
              k++;
            }
          }
          initialize(Integer.parseInt(widht), Integer.parseInt(hight));
          setCellSize(Integer.parseInt(tempCellSize));
          getAntModel().setField(tempField);
          repaint();
        }


      } finally {
        //closing of output stream
        in.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    setReplayByte(0);
    endLoadFile = true;
    try {
      FileWriter fstream = new FileWriter(replayFile);// конструктор с одним параметром - для перезаписи
      BufferedWriter out = new BufferedWriter(fstream); //  создаём буферезированный поток
      out.write(""); // очищаем, перезаписав поверх пустую строку
      out.close(); // закрываем
    } catch (Exception e)
    {System.err.println("Error in file cleaning: " + e.getMessage());}
  }

  public void saveForReplay() {
    byte[] tempField = ant.getField(); //Returns an array of current time

    //The existence check
    try {
      if (!replayFile.exists()) {
        replayFile.createNewFile();
      }

      PrintWriter out = new PrintWriter(new OutputStreamWriter
          (new FileOutputStream(replayFile.getAbsolutePath(), true), "UTF-8"));
      try { //write data to file
        out.println(replayFile.getName());
        out.println(Integer.toString(ant.getWidth()));
        out.println(Integer.toString(ant.getHeight()));
        out.println(Integer.toString(cellSize));
        out.println(Integer.toString(ant.getX()));
        out.println(Integer.toString(ant.getY()));
        out.println(Integer.toString(ant.getCourse()));

        for (int i = 0; i < ant.getHeight(); i++) {
          for (int g = 0; g < (ant.getWidth()); g++) {
            out.print(tempField[i * ant.getWidth() + g]);
          }
          out.println();
        }
      } finally {
        out.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  //Setting the cell size
  public void setCellSize(int s) {
    synchronized (ant) {
      ant.simulate();
    }
    cellSize = s;
  }

  public void setUpdateDelay(int updateDelay) {
    this.updateDelay = updateDelay;
  }

  //Run of the new stream
  public void startSimulation() {
    if (simThread == null) {
      simThread = new Thread(this);
      simThread.start();
    }
  }

  //Stop of the stream
  public void stopSimulation() {
    simThread = null;
  }

  //Test of work of the stream
  public boolean isSimulating() {
    return simThread != null;
  }

  public void saveToFile() {
    {
      byte[] tempField = ant.getField(); //Returns an array of current time

      //The existence check
      try {
        if (!saveFile.exists()) {
          saveFile.createNewFile();
        }

        PrintWriter out = new PrintWriter(new OutputStreamWriter
            (new FileOutputStream(saveFile.getAbsolutePath(), true), "UTF-8"));
        try { //write data to file
          out.println(saveFile.getName());
          out.println(Integer.toString(ant.getWidth()));
          out.println(Integer.toString(ant.getHeight()));
          out.println(Integer.toString(cellSize));
          out.println(Integer.toString(ant.getX()));
          out.println(Integer.toString(ant.getY()));
          out.println(Integer.toString(ant.getCourse()));
          for (int i = 0; i < ant.getHeight(); i++) {
            for (int g = 0; g < (ant.getWidth()); g++) {
              out.print(tempField[i * ant.getWidth() + g]);
            }
            out.println();
          }
        } finally {
          out.close();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  //simulation from file
  public void simulateFromFile() {
    String hight, widht, tempCellSize;
    byte[] tempField;

    try {
      BufferedReader in = new BufferedReader(new FileReader(loadFile.getAbsoluteFile()));
      try {
        char s[];
        String s1;

        while (true) {
          try {
            Thread.sleep(updateDelay);
          } catch (InterruptedException e) {
          }

          if (readByte == 0) break;
          if (in.readLine() == null) break;

          widht = in.readLine();
          hight = in.readLine();
          tempCellSize = in.readLine();
          ant.setX(Integer.parseInt(in.readLine()));
          ant.setY(Integer.parseInt(in.readLine()));
          ant.setCourse(Integer.parseInt(in.readLine()));
          tempField = new byte[Integer.parseInt(widht) * Integer.parseInt(hight)];
          int k = 0;

          for (int i = 0; i < Integer.parseInt(hight); i++) {
            s1 = in.readLine();
            s = s1.toCharArray();
            for (int g = 0; g < Integer.parseInt(widht); g++) {
              if (s[g] == '1') {
                tempField[k] = 1;
              } else {
                tempField[k] = 0;
              }
              k++;
            }
          }
          initialize(Integer.parseInt(widht), Integer.parseInt(hight));
          setCellSize(Integer.parseInt(tempCellSize));
          getAntModel().setField(tempField);
          repaint();
        }


      } finally {
        //closing of output stream
        in.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    setLoadByte(0);
    endLoadFile = true;
  }


  @Override
  public void run() {
    repaint();

    while (simThread != null) {
      try {
        Thread.sleep(updateDelay);
      } catch (InterruptedException e) {
      }
      /*
      * Synchronization is used to paintComponent method
      * is not displayed on the screen with the cell
      * which is changing now
      * */
      synchronized (ant) {
        if (replayByte == 1){
          replay();
          repaint();
        }
        if (readByte == 0) {
          if (saveByte == 1) {
            saveToFile();
          }
          saveForReplay();
          ant.simulate();
        } else {
          simulateFromFile();
        }
        repaint();
      }
    }
    repaint();
  }

  //Returns the size of the panel based on the field size and cells
  @Override
  public Dimension getPreferredSize() {
    if (ant != null) {
      Insets b = getInsets();
      return new Dimension((cellSize + cellInterval) * ant.getWidth()
          + cellInterval + b.left + b.right, (cellSize + cellInterval)
          * ant.getHeight() + cellInterval + b.top + b.bottom);
    } else
      return new Dimension(100, 100);
  }

  //Redrawing the content of pane
  @Override
  protected void paintComponent(Graphics g) {
    if (ant != null) {
      synchronized (ant) {
        super.paintComponent(g);
        Insets b = getInsets();
        for (int y = 0; y < ant.getHeight(); y++) {
          for (int x = 0; x < ant.getWidth(); x++) {
            byte c = (byte) ant.getCell(x, y);
            g.setColor(c == 1 ? c1 : c0);
            g.fillRect(b.left + cellInterval + x * (cellSize + cellInterval),
                b.top + cellInterval + y * (cellSize + cellInterval),
                cellSize, cellSize);
          }
        }
      }
    }
  }
}