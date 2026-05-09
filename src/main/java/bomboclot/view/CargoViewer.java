package bomboclot.view;

import bomboclot.algorithm.model.Position;
import bomboclot.algorithm.model.Prism;
import bomboclot.input.Dimensions;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CargoViewer extends Application
{
    private double anchorX;
    private double anchorY;

    private double angleX = -20;
    private double angleY = -30;

    private final Group world = new Group();

    private static ArrayList<PlacedPrism> prisms = new ArrayList<>();

    public static void set(ArrayList<PlacedPrism> prisms)
    {
        CargoViewer.prisms = prisms;
    }

    @Override
    public void start(Stage primaryStage)
    {
        // =========================================
        // TEST DATA
        // =========================================

        setPrisms(prisms);

        // =========================================
        // LIGHTING
        // =========================================

        AmbientLight ambient = new AmbientLight(Color.color(0.7, 0.7, 0.7));

        PointLight light = new PointLight(Color.WHITE);
        light.getTransforms().add(new Translate(-500, -500, -500));

        world.getChildren().addAll(ambient, light);

        // =========================================
        // CAMERA
        // =========================================

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100000);

        Rotate cameraRotateX = new Rotate(angleX, Rotate.X_AXIS);
        Rotate cameraRotateY = new Rotate(angleY, Rotate.Y_AXIS);

        Translate cameraDistance = new Translate(0, 0, -1000);

        camera.getTransforms().addAll(
            cameraRotateY,
            cameraRotateX,
            cameraDistance
        );

        // =========================================
        // SCENE
        // =========================================

        Scene scene = new Scene(
            new Group(world),
            1200,
            800,
            true
        );

        scene.setFill(Color.web("#202020"));
        scene.setCamera(camera);

        // =========================================
        // CAMERA CONTROLS
        // =========================================

        scene.setOnMousePressed((MouseEvent e) -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
        });

        scene.setOnMouseDragged((MouseEvent e) -> {

            double deltaX = e.getSceneX() - anchorX;
            double deltaY = e.getSceneY() - anchorY;

            angleY += deltaX * 0.3;
            angleX -= deltaY * 0.3;

            cameraRotateX.setAngle(angleX);
            cameraRotateY.setAngle(angleY);

            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
        });

        scene.setOnScroll(e -> {
            cameraDistance.setZ(
                cameraDistance.getZ() + e.getDeltaY()
            );
        });

        // =========================================
        // STAGE
        // =========================================

        primaryStage.setTitle("Cargo Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setPrisms(List<PlacedPrism> placedPrisms)
    {
        Random random = new Random(42);
        world.getChildren().clear();

        for (PlacedPrism placed : placedPrisms)
        {
            Prism prism = placed.prism();
            Position position = placed.position();

            Dimensions dimensions = prism.get_dimensions();

            Box box = new Box(
                dimensions.width(),
                dimensions.height(),
                dimensions.length()
            );

            // JavaFX Box is centered around origin,
            // so we offset by half size if your positions
            // represent minimum corner coordinates.

            box.getTransforms().add(
                new Translate(
                    position.x()
                        + dimensions.width() / 2.0,

                    position.y()
                        + dimensions.height() / 2.0,

                    position.z()
                        + dimensions.length() / 2.0
                )
            );

            PhongMaterial material = new PhongMaterial();

            material.setDiffuseColor(
                Color.color(
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble()
                )
            );

            box.setMaterial(material);

            world.getChildren().add(box);
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    public record PlacedPrism(
        Prism prism,
        Position position)
    {
    }
}