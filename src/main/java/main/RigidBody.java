package main;

import java.awt.Color;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

import Jama.Matrix;

//import Jama.Matrix;

//our simulation object
class RigidBody
{
	private class Wheel
    {
        private Vector2f m_forwardAxis, m_sideAxis;
        private float m_wheelTorque, m_wheelSpeed,
             m_wheelInertia, m_wheelRadius;
        private Vector2f m_Position = new Vector2f();

        public Wheel(Vector2f position, float radius)
        {
            m_Position = position;
            SetSteeringAngle(0);
            m_wheelSpeed = 0;
            m_wheelRadius = radius;
            m_wheelInertia = radius * radius; //fake value
        }

        public void SetSteeringAngle(float newAngle)
        {
        	float rads = (float)(newAngle*Math.PI/180.0f);
        	double[][] pts = {{Math.cos(rads),Math.sin(rads)},{-Math.sin(rads),Math.cos(rads)}};
            Matrix mat = new Matrix(pts);
            //Vector2f thing = relative.add(m_angle / (float)Math.PI *180.0f);
            Matrix multiply1 = new Matrix(new double[][]{{0},{1}});
            Matrix multiply2 = new Matrix(new double[][]{{-1},{0}});
            Matrix return1 = mat.times(multiply1);
            Matrix return2 = mat.times(multiply2);
            System.out.println(rads + " <--rads  return1");
            for(double[] i:pts){
            	for(double j:i){
            		System.out.print(j+",");
            	}
            	System.out.println("");
            }
            System.out.println("return2");
            for(double[] i:return2.getArray()){
            	for(double j:i){
            		System.out.print(j+",");
            	}
            	System.out.println("");
            }
            Vector2f vec1 = new Vector2f((float)return1.getArray()[0][0],(float) return1.getArray()[1][0]);
            Vector2f vec2 = new Vector2f((float)return2.getArray()[0][0],(float) return2.getArray()[1][0]);
            System.out.println("x,y" + vec1.x +","+vec1.y);
            System.out.println("x,y" + vec2.x +","+vec2.y);
            m_forwardAxis = vec1;
            m_sideAxis = vec2;
        }

        public void AddTransmissionTorque(float newValue)
        {
            m_wheelTorque += newValue;
        }

        public float GetWheelSpeed()
        {
            return m_wheelSpeed;
        }

        public Vector2f GetAttachPoint()
        {
            return m_Position;
        }

        public Vector2f CalculateForce(Vector2f relativeGroundSpeed,
            float timeStep)
        {
            //calculate speed of tire patch at ground
            Vector2f patchSpeed = new Vector2f(-m_forwardAxis.x,-m_forwardAxis.y).scale(m_wheelSpeed).scale(m_wheelRadius);

            //get velocity difference between ground and patch
            Vector2f velDifference = new Vector2f(relativeGroundSpeed).add(patchSpeed);

            //project ground speed onto side axis
            float forwardMag = 0;
            Vector2f sideVel = new Vector2f();
            Vector2f forwardVel = new Vector2f();
            velDifference.projectOntoUnit(m_sideAxis,sideVel);
            velDifference.projectOntoUnit(m_forwardAxis,forwardVel); //&forwardMag took that out, not sure what it's for, or how to put it in

            //calculate super fake friction forces
            //calculate response force
            Vector2f responseForce = new Vector2f(-sideVel.x,-sideVel.y).scale(2.0f);
            responseForce.sub(forwardVel);

            //calculate torque on wheel
            m_wheelTorque += forwardMag * m_wheelRadius;

            //integrate total torque into wheel
            m_wheelSpeed += m_wheelTorque / m_wheelInertia * timeStep;

            //clear our transmission torque accumulator
            m_wheelTorque = 0;

            //return force acting on body
            return responseForce;
        }
    }
	//linear properties
    private Vector2f m_position = new Vector2f();
    private Vector2f m_velocity = new Vector2f();
    private Vector2f m_forces = new Vector2f();
    private float m_mass;

    //angular properties
    private float m_angle;
    private float m_angularVelocity;
    private float m_torque;
    private float m_inertia;

    private Wheel [] wheels = new Wheel[4];
    //graphical properties
    private Vector2f m_halfSize = new Vector2f();
    Rectangle rect;
    private Color m_color;

    public RigidBody()
    { 
        //set these defaults so we dont get divide by zeros
        m_mass = 1.0f; 
        m_inertia = 1.0f; 
    }

    //intialize out parameters
    public void Setup(Vector2f halfSize, float mass, Color color,Vector2f position)
    {
    	//front wheels
        wheels[0] = new Wheel(new Vector2f(halfSize.x, halfSize.y), 0.5f);
        wheels[1] = new Wheel(new Vector2f(-halfSize.x, halfSize.y), 0.5f);

        //rear wheels
        wheels[2] = new Wheel(new Vector2f(halfSize.x, -halfSize.y), 0.5f);
        wheels[3] = new Wheel(new Vector2f(-halfSize.x, -halfSize.y), 0.5f);

        //store physical parameters
    	m_position = position;
        m_halfSize = halfSize;
        m_mass = mass;
        m_color = color;
        m_inertia = (1.0f / 12.0f) * (halfSize.x * halfSize.x)
             * (halfSize.y * halfSize.y) * mass;
        //generate our viewable rectangle
        rect = new Rectangle(m_halfSize.x,m_halfSize.y,m_halfSize.x * 2.0f,m_halfSize.y * 2.0f);
    }
    public void SetSteering(float steering)
    {
         float steeringLock = 0.75f;

        //apply steering angle to front wheels
        wheels[0].SetSteeringAngle(-steering * steeringLock);
        wheels[1].SetSteeringAngle(-steering * steeringLock);
    }

    public void SetThrottle(float throttle, boolean allWheel)
    {
         float torque = 20.0f;

        //apply transmission torque to back wheels
        if (allWheel)
        {
            wheels[0].AddTransmissionTorque(throttle * torque);
            wheels[1].AddTransmissionTorque(throttle * torque);
        }

        wheels[2].AddTransmissionTorque(throttle * torque);
        wheels[3].AddTransmissionTorque(throttle * torque);
    }

    public void SetBrakes(float brakes)
    {
         float brakeTorque = 4.0f;

        //apply brake torque apposing wheel vel
        for (Wheel wheel: wheels)
        {
            float wheelVel = wheel.GetWheelSpeed();
            wheel.AddTransmissionTorque(-wheelVel * brakeTorque * brakes);
        }
    }
    public void SetLocation(Vector2f position, float angle)
    {
        m_position = position;
        m_angle = angle;
    }
    public float GetAngle(){
    	return m_angle;
    }
    public Vector2f GetPosition()
    {
        return m_position;
    }

    public void Update(float timeStep)
    {
    	for (Wheel wheel: wheels)
        {
            Vector2f worldWheelOffset = RelativeToWorld(wheel.GetAttachPoint());
            Vector2f worldGroundVel = PointVel(worldWheelOffset);
            Vector2f relativeGroundSpeed = WorldToRelative(worldGroundVel);
            Vector2f relativeResponseForce = wheel.CalculateForce(relativeGroundSpeed, timeStep);
            Vector2f worldResponseForce = RelativeToWorld(relativeResponseForce);

            AddForce(worldResponseForce, worldWheelOffset);
        }
    	//System.out.println(timeStep);
        //integrate physics
        //linear
        Vector2f acceleration = (m_forces.scale(1/m_mass));
        m_velocity.add(acceleration);
       // System.out.println("velocity is " + m_velocity.x +"," + m_velocity.y + " position was"+m_position.x + ","+m_position.y);
        Vector2f oldvel = m_velocity;
        m_position.add(m_velocity.scale(timeStep));
        m_velocity = oldvel;
       // System.out.println("position is"+m_position.x + ","+m_position.y);
        m_forces = new Vector2f(0,0); //clear forces

        //angular
        float angAcc = m_torque / m_inertia;
        m_angularVelocity += angAcc;
        m_angle += m_angularVelocity * timeStep;
        m_torque = 0; //clear torque
        //Transform rotatetransform = Transform.createRotateTransform(m_angle);
        // rect.transform(rotatetransform);
        //Transform translatetransform = Transform.createTranslateTransform(m_position.x,m_position.y);
        //System.out.println(translatetransform);
        //translatetransform.
        //rect.transform(translatetransform);
    }
    //take a relative Vector2f and make it a world Vector2f
    public Vector2f RelativeToWorld(Vector2f relative)
    {
    	float rads = (m_angle);
    	double[][] pts = {{Math.cos(rads),Math.sin(rads)},{-Math.sin(rads),Math.cos(rads)}};
        Matrix mat = new Matrix(pts);
        //Vector2f thing = relative.add(m_angle / (float)Math.PI *180.0f);
        Matrix multiply = new Matrix(new double[][]{{relative.x},{relative.y}});
        Matrix return3 = mat.times(multiply);
      
        return new Vector2f((float)return3.getArray()[0][0],(float) return3.getArray()[1][0]);
    }

    //take a world vector and make it a relative vector
    public Vector2f WorldToRelative(Vector2f world)
    {
    	float rads = (-m_angle);
    	double[][] pts = {{Math.cos(rads),Math.sin(rads)},{-Math.sin(rads),Math.cos(rads)}};
        Matrix mat = new Matrix(pts);
        //Vector2f thing = relative.add(m_angle / (float)Math.PI *180.0f);
        Matrix multiply = new Matrix(new double[][]{{world.x},{world.y}});
        Matrix return3 = mat.times(multiply);
      
        return new Vector2f((float)return3.getArray()[0][0],(float) return3.getArray()[1][0]);
    
    }

    //velocity of a point on body
    public Vector2f PointVel(Vector2f worldOffset)
    {
        Vector2f tangent = new Vector2f(-worldOffset.y, worldOffset.x);
        return tangent.scale(m_angularVelocity).add(m_velocity);
    }

    public void AddForce(Vector2f worldForce, Vector2f worldOffset)
    {
        //add linar force
        m_forces.add(worldForce);
        //and it's associated torque
        m_torque += worldOffset.dot(worldForce);
    }
}