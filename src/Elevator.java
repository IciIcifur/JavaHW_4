import java.awt.*;
import java.util.ArrayList;

public class Elevator {
    int target; // floor elevator tries to arrive at
    Size size; // capacity of elevator
    int fullness = 0; // number of people in elevator
    volatile ArrayList<Person> peopleInElevator = new ArrayList<>();
    // elevator's position
    int x;
    int y;
    int floor = 1;
    // elevator's params
    static int height = 76;
    int width;
    int speed;

    Elevator(int xPosition, int yPosition, Size size) {
        this.x = xPosition;
        this.y = yPosition;
        this.size = size;

        this.width = size.width;
        this.speed = -1;
    }

    /**
     * Draws an elevators, allows people in it to move, draws people in it.
     */
    void paint(Graphics g) {
        if (g == null) return;
        g.setColor(Color.white);
        g.fillRect(this.x, this.y, this.width, height);
        g.setColor(Color.gray);
        g.drawRect(this.x, this.y, this.width, height);
        g.drawString(String.valueOf(this.floor), this.x + 5, this.y + 15);
        g.setColor(Color.red);
        g.drawString(String.valueOf(this.fullness), this.x + this.width - 15, this.y + 15);

        for (int i = 0; i < this.peopleInElevator.size(); i++) {
            this.peopleInElevator.get(i).y = this.y + Elevator.height - Person.height;
            this.peopleInElevator.get(i).go(this.x, this.x + this.width);
            this.peopleInElevator.get(i).paint(g);
        }
    }

    /**
     * Defines parameters of an elevator with specific size (type).
     */
    public enum Size {
        LARGE(6, 70),
        SMALL(3, 55);

        public final int number_of_people;
        public final int width;

        Size(int n, int width) {
            this.number_of_people = n;
            this.width = width;
        }
    }
}
