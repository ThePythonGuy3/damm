package bomboclot.view;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class CargoViewer extends Application
{
    private double anchorX;
    private double anchorY;

    private double angleX = -20;
    private double angleY = -30;

    @Override
    public void start(Stage primaryStage)
    {
        // =========================
        // WORLD
        // =========================

        Group world = new Group();

        Box box = new Box(100, 200, 50);

        Translate transform = new Translate(200, 150, 0);

        Rotate rotate = new Rotate();
        rotate.setAxis(new Point3D(0.2, 0.4, 0.6));
        rotate.setAngle(25);

        box.getTransforms().addAll(transform, rotate);

        world.getChildren().add(box);

        // =========================
        // CAMERA
        // =========================

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        // Camera pivot rotations
        Rotate cameraRotateX = new Rotate(angleX, Rotate.X_AXIS);
        Rotate cameraRotateY = new Rotate(angleY, Rotate.Y_AXIS);

        // Move camera backwards so we can see the scene
        Translate cameraDistance = new Translate(0, 0, -800);

        camera.getTransforms().addAll(
            cameraRotateY,
            cameraRotateX,
            cameraDistance
        );

        // =========================
        // ROOT
        // =========================

        Group root = new Group(world);

        Scene scene = new Scene(root, 400, 300, true);

        scene.setFill(Color.web("#ffe456"));
        scene.setCamera(camera);

        // =========================
        // MOUSE CONTROLS
        // =========================

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

        // =========================
        // STAGE
        // =========================

        primaryStage.setTitle("Cargo Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}