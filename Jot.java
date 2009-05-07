import java.io.*;

class Jot {
		Joint joint;
		public Jot(){
			joint = new Joint("");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (true){
                int i = 0;
                System.out.print("Jot:$");
                try {
					String s = br.readLine();
					if (s.equals("list")){
					}else if (s.equals("show")){
						System.out.println(joint.getName());
					}else if (s.equals("")){
						System.out.println(joint.getName());
					}else if (s.equals("exit")){
						System.exit(0);
					}else{	
						joint = new Joint(s);
						System.out.println(joint.getName());
					}	
                }catch (IOException err) {
                        System.out.println(err);
                }
			}
		}
        public static void main(String args[]) {
			new Jot();
        }
}
class Joint{
	Class _class;
	public Joint(String s){
		try{
			ClassLoader classLoader = this.getClass().getClassLoader();
			_class = classLoader.loadClass(s);
		}catch(Throwable th){
			_class = this.getClass();
		}
	}
    public String getName(){
		return _class.getName();
	}
}