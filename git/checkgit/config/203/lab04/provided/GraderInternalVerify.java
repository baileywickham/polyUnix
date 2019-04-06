
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public class GraderInternalVerify {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            Main.main(args);
        } else if (args.length == -1) {
            Point pt = new Point(1.2, 3.4);
            Color color = null;
            double d = 5.6;
            d = pt.x;
            d = pt.y;

            Circle circle = new Circle(d, pt, color);
            color = circle.getColor();
            circle.setColor(color);
            pt = circle.getCenter();
            d = circle.getRadius();
            circle.setRadius(d);
            d=circle.getArea();
            d=circle.getPerimeter();
            circle.translate(d, d);

            ConvexPolygon polygon = new ConvexPolygon(new Point[0], color);
            color = polygon.getColor();
            polygon.setColor(color);
            pt = polygon.getVertex(-1);
            int i = polygon.getNumVertices();
            d = polygon.getArea();
            d = polygon.getPerimeter();
            polygon.translate(d, d);

            Rectangle rectangle = new Rectangle(d, d, pt, color);
            color = rectangle.getColor();
            rectangle.setColor(color);
            d = rectangle.getWidth();
            rectangle.setWidth(d);
            d = rectangle.getHeight();
            rectangle.setHeight(d);
            d = rectangle.getArea();
            d = rectangle.getPerimeter();
            rectangle.translate(d, d);

            Triangle triangle = new Triangle(pt, pt, pt, color);
            pt = triangle.getVertexA();
            pt = triangle.getVertexB();
            pt = triangle.getVertexC();


            Shape shape = circle;
            shape = rectangle;
            shape = polygon;
            shape = triangle;
            color = shape.getColor();
            shape.setColor(color);
            d = shape.getArea();
            d = shape.getPerimeter();
            shape.translate(d, d);

            WorkSpace workspace = new WorkSpace();
            List<Shape> sl = null;
            workspace.add(shape);
            shape=workspace.get(0);
            i = workspace.size();
            List<Circle> cl = workspace.getCircles();
            List<Rectangle> rl = workspace.getRectangles();
            List<Triangle> tl = workspace.getTriangles();
            List<ConvexPolygon> cpl = workspace.getConvexPolygons();
            sl = workspace.getShapesByColor(Color.red);
            d = workspace.getAreaOfAllShapes();
            d = workspace.getPerimeterOfAllShapes();
        }
    }
}
