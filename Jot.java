import java.io.*;
import java.util.*;
import java.lang.reflect.*;
class Jot {
		Joint joint;
		Iterator i,ii;
		public Jot(){
			joint = new Joint("");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (true){
                System.out.print(joint.getName()+":$");
                try {
					String s = br.readLine();
					if (s.equals("ls")){
						/*
						List a = Arrays.asList(System.getProperty("java.class.path").split(System.getProperty("path.separator")));
				        for (i = a.iterator(); i.hasNext();) {
				            String ss = (String)i.next();
							File f = new File(ss);
							if (f.isDirectory()){
								List aa  = Arrays.asList(f.list());
						        for (ii = aa.iterator(); ii.hasNext();) {
						            String sss = (String)ii.next();
									System.out.println(sss);					        	
						        }
							}
				        }
*/
				        for (i = joint.list().iterator(); i.hasNext();) {
				            Method m = (Method)i.next();
				        	System.out.println(m.getName()); //変更がList側にも反映
				        }
        			}else if (s.equals("show")){
						System.out.println(joint.getName());
        			}else if (s.equals("cons")){
				        for (i = joint.cons().iterator(); i.hasNext();) {
				            Constructor c = (Constructor)i.next();
				        	System.out.println(c.toString()); //変更がList側にも反映
						}
        			}else if (s.equals("path")){
						System.out.println(System.getProperty("java.class.path"));
        			}else if (s.equals("two")){
						joint = new Joint("Test");
        				joint.createObject();
						System.out.println(joint.invoke());
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
class Test{
	public String two(int i){
		return ""+i*2;
	}
}
class Joint{
	Class _class;
	Object _object;
	public Joint(String s,int i){}
	public Joint(String s){
		try{
			ClassLoader classLoader = this.getClass().getClassLoader();
			_class = classLoader.loadClass(s);
		}catch(Throwable th){
			if (_class == null){
				_class = this.getClass();
			}
		}
	}
	public void createObject(){
		try{
			_object =_class.getConstructors()[0].newInstance(null);
		}catch(Throwable th){
		}
	}
	public Object invoke(){
		Class argTypes[] = { int.class };
		int i = 10;
		Object params[] = {new Integer(i)};
		try{
			_object =_class.newInstance();
			return _class.getDeclaredMethod("two",argTypes).invoke(_object,params);
		}catch(Throwable th){
            System.out.println(th.toString());
			return null;
		}
	}

    public String getName(){
		return _class.getName();
	}
	public List list(){
		return java.util.Arrays.asList(_class.getDeclaredMethods());		
	}
	public List cons(){
		return java.util.Arrays.asList(_class.getConstructors());		
	}

}