import java.awt.*;
import java.util.ArrayList;

public class Elevator {
    int target; // floor elevator wants to arrive at
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

    synchronized void paint(Graphics g) {
        if (g == null) return;
        g.setColor(Color.white);
        g.fillRect(this.x, this.y, this.width, height);
        g.setColor(Color.gray);
        g.drawRect(this.x, this.y, this.width, height);
        g.drawString(String.valueOf(this.floor), this.x + 5, this.y + 15);
        g.setColor(Color.red);
        g.drawString(String.valueOf(this.fullness), this.x + this.width - 15, this.y + 15);

        for (Person person : this.peopleInElevator) {
            person.paint(g);
            person.y = this.y + Elevator.height - Person.height;
            person.go(this.x, this.x + this.width);
        }
    }

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
