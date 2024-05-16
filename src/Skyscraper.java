import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Skyscraper {
    static int maximumPeopleOnFloor = 5;
    static int width = 400;
    static Random random = new Random();
    int floorHeight = 76;
    int floorsNumber;
    int height;
    int x1, x2, y1, y2;
    int attic = 24, elevatorsBoxWidth = 155;
    int elevatorsBoxX1, elevatorsBoxX2;

    ArrayList<ArrayList<Person>> peopleOnFloors;

    Skyscraper(int xPosition, int yPosition, int floorsNumber) {
        this.floorsNumber = floorsNumber;
        this.height = floorHeight * floorsNumber + this.attic;

        this.x1 = xPosition;
        this.x2 = this.x1 + width;
        this.y2 = yPosition;
        this.y1 = this.y2 - this.height;

        this.peopleOnFloors = new ArrayList<>(floorsNumber);
        for (int i = 1; i <= floorsNumber + 1; i++) {
            this.peopleOnFloors.add(new ArrayList<>());
        }

        this.elevatorsBoxX1 = this.x1 + (width - this.elevatorsBoxWidth) / 2;
        this.elevatorsBoxX2 = this.x1 + (width - this.elevatorsBoxWidth) / 2 + this.elevatorsBoxWidth;
    }

    void newPerson() {
        int currentFloor = 1 + random.nextInt(this.floorsNumber);
        int destinationFloor = 1 + random.nextInt(this.floorsNumber);
        if (this.peopleOnFloors.get(currentFloor).size() < maximumPeopleOnFloor && currentFloor != destinationFloor && random.nextInt(100) < 5) {
            int currentY = this.y1 + 1 + this.attic + currentFloor * this.floorHeight - Person.height;
            int currentX = this.x1 + 1 + random.nextInt(width - Person.width);
            this.peopleOnFloors.get(currentFloor).add(new Person(destinationFloor, currentX, currentY));
        }
    }

    void paint(Graphics g, int windowH, int windowW) {
        // sky
        g.setColor(new Color(170, 213, 239));
        g.fillRect(0, 0, windowW, windowH);
        // grass
        g.setColor(new Color(184, 203, 142));
        g.fillRect(0, windowH - 20, windowW,20);

        // house
        g.setColor(Color.white);
        g.fillRect(this.x1, this.y1, width, this.height);

        // elevators' box
        g.setColor(Color.lightGray);
        g.fillRect(this.elevatorsBoxX1, this.y1, this.elevatorsBoxWidth, this.height);

        // floors
        int floorX1 = this.x1;
        int floorX2 = this.x1 + width;
        for (int i = 1; i <= this.floorsNumber; i++) {
            int floorH = this.y1 + this.attic + i * this.floorHeight;
            int currentFloor = this.floorsNumber - i + 1;
            g.setColor(Color.black);
            g.drawString(String.valueOf(currentFloor), floorX1 - 20, floorH - 15);
            g.drawLine(floorX1, floorH, floorX2, floorH);
        }

        // house line
        g.setColor(Color.black);
        g.drawRect(this.x1, this.y1, width, this.height);
    }
}
