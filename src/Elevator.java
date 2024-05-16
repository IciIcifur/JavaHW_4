import java.awt.*;
import java.util.ArrayList;

public class Elevator {
    Size size; // capacity of elevator
    int fullness = 0; // number of people in elevator
    ArrayList<Person> peopleInElevator = new ArrayList<>();
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
        this.speed = size.speed;
    }

    void takePerson(Person person) {
        this.peopleInElevator.add(person);
    }

    ArrayList<Person> newFloor() {
        ArrayList<Person> arrived = new ArrayList<>();
        for (Person person : this.peopleInElevator) {
            if (person.destinationFloor == this.floor) {
                arrived.add(person);
                this.peopleInElevator.remove(person);
            }
        }

        return arrived;
    }

    void paint(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(this.x, this.y, this.width, this.height);
        g.setColor(Color.gray);
        g.drawRect(this.x, this.y, this.width, this.height);
        g.drawString(String.valueOf(this.floor), this.x + 5, this.y + 15);
        g.setColor(Color.red);
        g.drawString(String.valueOf(this.fullness), this.x + this.width - 15, this.y + 15);

        for (Person person : this.peopleInElevator) {
            person.y = this.y - Person.height;
            person.paint(g);
        }
    }

    public enum Size {
        LARGE(6, 60, 1),
        SMALL(3, 50, 2);

        public final int number_of_people;
        public final int width;
        public final int speed;

        Size(int n, int width, int speed) {
            this.number_of_people = n;
            this.width = width;
            this.speed = speed;
        }
    }
}
