/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JME3 Classes/Control.java to edit this template
 */
package hello;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author foggia
 */
public class MeteorControl extends AbstractControl {
    //Any local variables should be encapsulated by getters/setters so they
    //appear in the SDK properties window and can be edited.
    //Right-click a local variable to encapsulate it with getters and setters.
    private static final float SPEED_INCREASE_MIN=0.15f,
                               SPEED_INCREASE_MAX=0.2f,
                               SPEED_CHANGE_TIME=10.0f;
    private static final float SPEED_CORRECTION_FACTOR=5.0f;
    private Spatial dino;
    private Vector3f offset=new Vector3f();
    private Vector3f vel=new Vector3f();
    private float speed=0.0f;
    private float time=0.0f;
    
    public MeteorControl(Node rootNode) {
        dino=rootNode.getChild("Dino");
    }

    @Override
    protected void controlUpdate(float tpf) {
        Spatial meteor=getSpatial();
        RigidBodyControl rbc=meteor.getControl(RigidBodyControl.class);
        offset.set(dino.getLocalTranslation());
        offset.subtractLocal(meteor.getLocalTranslation());
        
        offset.normalizeLocal();
        rbc.getLinearVelocity(vel);
        float actual_speed=offset.dot(vel);
        float delta=speed-actual_speed;
        if (delta<0.0f)
            delta=0.0f;

        offset.multLocal(delta*rbc.getMass()*SPEED_CORRECTION_FACTOR);
        rbc.applyCentralForce(offset);
        
        time += tpf;
        if (time>SPEED_CHANGE_TIME) {
            time=0.0f;
            speed += FastMath.nextRandomFloat()*(SPEED_INCREASE_MAX-SPEED_INCREASE_MIN)
                     + SPEED_INCREASE_MIN;
        }
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }
    
}
