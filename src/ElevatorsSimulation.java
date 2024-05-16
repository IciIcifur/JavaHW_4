import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ElevatorsSimulation extends JFrame {
    Skyscraper house;
    Elevator eLarge;
    Elevator eSmall;

    ArrayList<Person> disappearing = new ArrayList<>();

    final String TITLE_OF_PROGRAM = "Elevators Simulation";


    public static void main(String[] args) {
        new ElevatorsSimulation();
    }

    public ElevatorsSimulation() {

        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setBackground(Color.white);
        canvas.setPreferredSize(
                new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        add(canvas);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);


        this.house = new Skyscraper((WINDOW_WIDTH - Skyscraper.width) / 2, (WINDOW_HEIGHT - 10), 8);
        this.eLarge = new Elevator(this.house.elevatorsBoxX1 + 10, house.y2 - Elevator.height, Elevator.Size.LARGE);
        this.eSmall = new Elevator(this.house.elevatorsBoxX2 - 10 - Elevator.Size.SMALL.width, house.y2 - Elevator.height, Elevator.Size.SMALL);

        while (true) {
            eLarge.y -= eLarge.speed;
            eLarge.floor = this.house.floorsNumber - eLarge.y / this.house.floorHeight;
            eSmall.y -= eSmall.speed;
            eSmall.floor = this.house.floorsNumber - eSmall.y / this.house.floorHeight;
            canvas.repaint();
            if (eSmall.y <= house.y1 + house.attic || eSmall.y >= house.y2 - Elevator.height) eSmall.speed *= -1;
            if (eLarge.y <= house.y1 + house.attic || eLarge.y >= house.y2 - Elevator.height) eLarge.speed *= -1;

            for (ArrayList<Person> floor : this.house.peopleOnFloors)
                for (Person person : floor) person.go(house.x1, house.x2);

            try {
                Thread.sleep(FPS / 6);
                this.house.newPerson();
                this.disappearing.removeIf(person -> person.disappear(person.destinationFloor == 1 ? 0 : this.house.x1));
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);

            house.paint(g, WINDOW_HEIGHT, WINDOW_WIDTH);
            for (ArrayList<Person> floor : house.peopleOnFloors) {
                for (Person person : floor) {
                    person.paint(g);
                }

            }
            eLarge.paint(g);
            eSmall.paint(g);
        }
    }

    public final int WINDOW_WIDTH = 650;
    public final int WINDOW_HEIGHT = 650;
    public final int FPS = 60;
}