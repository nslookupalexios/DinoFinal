/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JME3 Classes/Control.java to edit this template
 */
package hello;

import com.jme3.anim.AnimComposer;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author foggia
 */
public class MotionControl extends AbstractControl implements ActionListener {
    //Any local variables should be encapsulated by getters/setters so they
    //appear in the SDK properties window and can be edited.
    //Right-click a local variable to encapsulate it with getters and setters.
    private static final float OMEGA=2*FastMath.PI/10;
    private static final float VELOCITY=3.0f;
    private static final float ANIM_SPEED_RATIO=0.4f;
    private static final float ANGLE_CORRECTION_FACTOR=1.0f;
    private Vector3f s=new Vector3f();
    private Vector3f w=new Vector3f();
    private AnimComposer animComposer=null;
    private boolean moveForward=false, moveBackward=false,
            rotateLeft=false, rotateRight=false;
    private String currentAction=null;
    private Quaternion quaternion=new Quaternion();
    private float[] angles=new float[3];
    
    public MotionControl(InputManager inputManager) {
        inputManager.addListener(this, "Forward", 
                "Backward",
                "RotateLeft", "RotateRight");
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (moveForward) {
            setAction("run", VELOCITY*ANIM_SPEED_RATIO);
            forward(1.0f);
        } else if (moveBackward) {
            setAction("run", -0.5f*VELOCITY*ANIM_SPEED_RATIO);
            forward(-0.5f);
        }  else {
            setAction("idle", 1.0f);
        }
        
        if (rotateLeft) 
            rotate(1.0f);
        else if (rotateRight)
            rotate(-1.0f);
        
        straightenUp();
    }
    
    private void forward(float value) {
        float d=VELOCITY*value;
        s.set(0f, 0f, d);
        Spatial dino=getSpatial();
        
        Quaternion localRotation=dino.getLocalRotation();
        localRotation.mult(s, w);
        
        RigidBodyControl rbc=dino.getControl(RigidBodyControl.class);
        rbc.getLinearVelocity(s);
        w.y = s.y;
        
        rbc.setLinearVelocity(w);
    }
    
    private void setAction(String action, float speed) {
        if (action.equals(currentAction))
            return;
        currentAction=action;
        AnimComposer a=getAnimComposer();
        if (a!=null) {
            
            a.setCurrentAction(action);
            a.setGlobalSpeed(speed);
        }
    }
    
    private void rotate(float value) {
        float ang=OMEGA*value;
        Spatial dino=getSpatial();
        RigidBodyControl rbc=dino.getControl(RigidBodyControl.class);
        rbc.getAngularVelocity(s);
        s.y=ang;
        rbc.setAngularVelocity(s);
        
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public void onAction(String input, boolean active, float tpf) {
        if (!isEnabled())
            return;
        if (input.equals("Forward")) 
            moveForward=active;
        else if (input.equals("Backward"))
            moveBackward=active;
        else if (input.equals("RotateLeft"))
            rotateLeft=active;
        else if (input.equals("RotateRight"))
            rotateRight=active;
    }
    
    private AnimComposer getAnimComposer() {
        if (animComposer==null) {
            animComposer=findAnimComposer(getSpatial());
        }
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
        moveForward=false;
        moveBackward=false;
        rotateLeft=false;
        rotateRight=false;
        setAction("idle", 1.0f);
        
    }
    
    private void straightenUp() {
        Spatial dino=getSpatial();
        RigidBodyControl rbc=dino.getControl(RigidBodyControl.class);
        rbc.getPhysicsRotation(quaternion);
        quaternion.toAngles(angles);
        float ang_x=normalize(angles[0]);
        float ang_z=normalize(angles[2]);
        float mass=rbc.getMass();
        w.set(-ang_x*mass*ANGLE_CORRECTION_FACTOR,
              0.0f,
              -ang_z*mass*ANGLE_CORRECTION_FACTOR);
        rbc.applyTorque(w);
    }
    
    private float normalize(float ang) {
        while (ang<-FastMath.PI)
            ang += 2.0f*FastMath.PI;
        while (ang>FastMath.PI)
            ang -= 2.0f*FastMath.PI;
        return ang;
    }
    
}
