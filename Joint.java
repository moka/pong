import java.lang.reflect.*;

class Joint{
		public Joint(){
				System.out.println("test");
		}
    public static void main(String[] args){
				Joint joint = new Joint();
				ClassLoader classLoader = joint.getClass().getClassLoader();
				try{
						Class testClass = classLoader.loadClass("Ball");
						Constructor[] constructors  = testClass.getConstructors();
						for (int i = 0; i < constructors.length; i++) {
                System.out.println("   " + constructors[i].toString());
            }						
						System.out.println(testClass.toString());
						Method[] methods = testClass.getDeclaredMethods();
						for (int i = 0; i < methods.length; i++) {
                System.out.println("   " + methods[i].toString());
            }

				}catch(Throwable th){
				}
    }
}