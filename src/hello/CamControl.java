package hello;

import com.jme3.input.InputManager;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CamControl extends AbstractControl implements AnalogListener {

    private final Camera camera;
    private static final Vector3f offset = new Vector3f(0f, 1.5f, 5.5f);
    private static final Vector3f pos = new Vector3f();
    private static final Quaternion rotation = new Quaternion();
    private static final float OMEGA=2.0f;

    private static final Map<String, BiConsumer<Float, Quaternion>> actionsMap = new HashMap<>() {{
        put("CamLeft",  (value, rotation) -> rotation.fromAngles(0f,  OMEGA*value, 0f).multLocal(offset));
        put("CamRight", (value, rotation) -> rotation.fromAngles(0f, -OMEGA*value, 0f).multLocal(offset));
        put("CamUp",    (value, rotation) -> rotation.fromAngles(-OMEGA*value, 0f, 0f).multLocal(offset));
        put("CamDown",  (value, rotation) -> rotation.fromAngles(OMEGA*value, 0f, 0f).multLocal(offset));
    }};

    public CamControl(Camera camera, InputManager inputManager) {
        this.camera=camera;
        inputManager.addListener(this, "CamLeft", "CamRight", "CamUp", "CamDown");
    }

    @Override
    protected void controlUpdate(float tpf) {
        Spatial dino=getSpatial();
        pos.set(dino.getLocalTranslation());
        pos.addLocal(offset);
        camera.setLocation(pos);
        
        pos.set(dino.getLocalTranslation());
        pos.y += 1.0f;
        camera.lookAt(pos, Vector3f.UNIT_Y);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public void onAnalog(String input, float value, float tpf) {
        BiConsumer<Float, Quaternion> action = actionsMap.get(input);
        if(action != null) {
            action.accept(value, rotation);
        }
    }
}