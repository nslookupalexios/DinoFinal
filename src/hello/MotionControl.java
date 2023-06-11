package hello;

import com.jme3.anim.AnimComposer;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class MotionControl extends AbstractControl implements ActionListener {

    private static final float OMEGA = 2 * FastMath.PI / 10;
    private static final float VELOCITY = 8.0f;
    private static final float ANIM_SPEED_RATIO = 0.4f;
    private static final float ANGLE_CORRECTION_FACTOR = 1.0f;
    private static final Vector3f jumpForce = new Vector3f(0, 10000f, 0);

    private final Vector3f s = new Vector3f();
    private final Vector3f w = new Vector3f();
    private final RigidBodyControl rbc;
    private final Quaternion quaternion = new Quaternion();
    private final float[] angles = new float[3];

    private final AnimComposer animComposer;
    private boolean moveForward = false, moveBackward = false,
            rotateLeft = false, rotateRight = false, jump = false;
    private String currentAction = null;

    public MotionControl(InputManager inputManager, RigidBodyControl rbc) {
        this.rbc = rbc;
        this.animComposer = findAnimComposer(rbc.getSpatial());

        inputManager.addListener(this, "Forward",
                "Backward",
                "RotateLeft", "RotateRight", "Jump");
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (moveForward) {
            setAction("run", VELOCITY * ANIM_SPEED_RATIO);
            forward(1.0f);
        } else if (moveBackward) {
            setAction("run", -0.5f * VELOCITY * ANIM_SPEED_RATIO);
            forward(-0.5f);
        } else {
            setAction("idle", 1.0f);
        }

        if (rotateLeft) {
            rotate(1.0f);
        } else if (rotateRight) {
            rotate(-1.0f);
        }

        if (jump) {
            jumpAction();
        }

        straightenUp();
    }

    private void jumpAction() {
        System.out.println("JUMPED");
        this.rbc.applyImpulse(jumpForce, Vector3f.ZERO);
        this.jump = false;
    }

    private void forward(float value) {
        float d = VELOCITY * value;
        s.set(0f, 0f, d);
        Spatial dino = getSpatial();
        Quaternion localRotation = dino.getLocalRotation();
        localRotation.mult(s, w);

        this.rbc.getLinearVelocity(s);
        w.y = s.y;

        this.rbc.setLinearVelocity(w);
    }

    private void setAction(String action, float speed) {
        if (action.equals(currentAction)) {
            return;
        }
        currentAction = action;
        AnimComposer a = getAnimComposer();
        if (a != null) {

            a.setCurrentAction(action);
            a.setGlobalSpeed(speed);
        }
    }

    private void rotate(float value) {
        float ang = OMEGA * value;
        this.rbc.getAngularVelocity(s);
        s.y = ang;
        this.rbc.setAngularVelocity(s);

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public void onAction(String input, boolean active, float tpf) {
        if (!isEnabled()) {
            return;
        }
        if (input.equals("Forward")) {
            moveForward = active;
        } else if (input.equals("Backward")) {
            moveBackward = active;
        } else if (input.equals("RotateLeft")) {
            rotateLeft = active;
        } else if (input.equals("RotateRight")) {
            rotateRight = active;
        } else if (input.equals("Jump")) {
            jump = active;
        }
    }

    private AnimComposer getAnimComposer() {
        return animComposer;
    }

    private AnimComposer findAnimComposer(Spatial s) {
        AnimComposer composer = s.getControl(AnimComposer.class);
        if (composer != null) {
            return composer;
        }
        if (s instanceof Node) {
            Node node = (Node) s;
            for (Spatial child : node.getChildren()) {
                composer = findAnimComposer(child);
                if (composer != null) {
                    return composer;
                }
            }
        }
        return null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        moveForward = false;
        moveBackward = false;
        rotateLeft = false;
        rotateRight = false;
        setAction("idle", 1.0f);

    }

    private void straightenUp() {
        this.rbc.getPhysicsRotation(quaternion);
        quaternion.toAngles(angles);
        float ang_x = normalize(angles[0]);
        float ang_z = normalize(angles[2]);
        float mass = this.rbc.getMass();
        w.set(-ang_x * mass * ANGLE_CORRECTION_FACTOR,
                0.0f,
                -ang_z * mass * ANGLE_CORRECTION_FACTOR);
        rbc.applyTorque(w);
    }

    private float normalize(float ang) {
        while (ang < -FastMath.PI) {
            ang += 2.0f * FastMath.PI;
        }
        while (ang > FastMath.PI) {
            ang -= 2.0f * FastMath.PI;
        }
        return ang;
    }

}
