import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import gnu.getopt.Getopt;
import jline.*;

class Jot {
	Joint joint;List a = null;Iterator i,ii;String s=null;
	public Jot(){
        try {
			joint = new Joint("");
        	ConsoleReader reader = new ConsoleReader(System.in, 
        		new PrintWriter(
        				new OutputStreamWriter(System.out,
        						System.getProperty("jline.WindowsTerminal.output.encoding",System.getProperty("file.encoding")))));
    		reader.setBellEnabled(false);
	        List completors = new LinkedList();
            completors.add(new SimpleCompletor(new String[] { "foo", "bar",
                    "baz" }));
            reader.addCompletor(new ArgumentCompletor(completors));
        	while ((s = reader.readLine("[jot]"+joint.getName()+":$")) != null) {
				if (s.matches("^ls.*")){
					boolean is_detail = false;
					boolean is_all = false;
					Getopt options = new Getopt("ls", s.split(" ") , "al");
			        int c;
			        while ( (c = options.getopt()) != -1) {
			            switch (c) {
			            case 'a':
							is_all = true;
			                break;
			            case 'l':
							is_detail = true;
			                break;
			            default:
			            }
			        }
		            if (is_all){
						a = Arrays.asList(joint.getClassObject().getMethods());												
					}else{
						a = Arrays.asList(joint.getClassObject().getDeclaredMethods());										
					}
					for (i = a.iterator(); i.hasNext();) {
			            Method m = (Method)i.next();
			            if (is_detail){
				        	System.out.println(m.toString()); 
						}else{
				        	System.out.println(m.getName()); 
						}
			        }
    			}else if (s.equals("classes")){
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
				}else if (s.matches("^cd.*")){
					joint = new Joint(s.split(" ")[1]);
					System.out.println(joint.getName());
    			}else if (s.equals("show")){
					System.out.println(joint.getName());
    			}else if (s.equals("cons")){
			        for (i = joint.cons().iterator(); i.hasNext();) {
			            Constructor c = (Constructor)i.next();
			        	System.out.println(c.toString()); 
					}
    			}else if (s.equals("path")){
					System.out.println(System.getProperty("java.class.path"));
    			}else if (s.equals("two2")){
					joint = new Joint("Test");
					System.out.println(joint.invoke());
				}else if (s.equals("")){
					System.out.println(joint.getName());
				}else if (s.equals("exit")){
					System.exit(0);
				}else{	
					//method name argments
					ArrayList a = joint.search(s.split(" ")[0]);
					if (a == null){
						System.out.println("OK");
					}else{
						System.out.println("NG");
					}					
					//int i = Integer.valueOf(s.split(" ")[1]).intValue();
					//Object params[] = {new Integer(i)};
					//System.out.println(joint.invoke(s.split(" ")[0],params));

				}	
			}
		}catch(Throwable t){
			System.out.println(t.toString());
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
	public String three(int i){
		return ""+i*3;
	}
}
class Joint{
	Class _class;
	Object _object;
	public Class getClassObject(){
		return _class;
	}
	public Joint(String s,int i){}
	public ArrayList  search(String method_name){
		List a = Arrays.asList(_class.getDeclaredMethods());		
		ArrayList  return_methods= new ArrayList();
		Iterator i = null;
		for (i = a.iterator(); i.hasNext();) {
            Method m = (Method)i.next();
			if (m.getName().equals(method_name)){
				try{
					return_methods.add(m);
				}catch(Throwable t){
					System.out.println(t.toString());
				}
			}
		}
		return return_methods;
	}
	public Joint(String s){
		try{
			ClassLoader classLoader = this.getClass().getClassLoader();
			_class = classLoader.loadClass(s);
		}catch(Throwable th){
			if (_class == null){
				_class = this.getClass();
			}
		}
		try{
			_object =_class.newInstance();
		}catch(Throwable th){
		}
	}
	public Object invoke(){
		Class argTypes[] = { int.class };
		int i = 10;
		Object params[] = {new Integer(i)};
		try{
			return _class.getDeclaredMethod("two",argTypes).invoke(_object,params);
		}catch(Throwable th){
            System.out.println(th.toString());
			return null;
		}
	}

	public Object invoke(String method_name,Object params[]){
		Class argTypes[] = { int.class };
		try{
			return _class.getDeclaredMethod(method_name,argTypes).invoke(_object,params);
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