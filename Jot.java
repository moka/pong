import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import gnu.getopt.Getopt;
import jline.*;
 
class Jot {
	Joint joint;List a = null;Iterator i,ii;
	static final String _config_file_name = ".config";
	public Jot(String in_args[]) throws Throwable{
			String s="";
			//config file create
			if(!new File(this._config_file_name).exists()){new File(this._config_file_name).createNewFile();}

			//java option
			int c; Getopt options = new Getopt("java Jot", in_args , "s");
	        while ( (c = options.getopt()) != -1) {if (c == 's'){System.out.println("stdin");}}

			//java env
			if (new File(".config").exists()){joint = new Joint(new BufferedReader(new InputStreamReader(new FileInputStream(new File(".config")))).readLine());}else{joint = new Joint("");}

			//main loop
	    	if (System.console() != null) {	
	    		//stdin is from keyboard
				ConsoleReader reader = new ConsoleReader(System.in,new PrintWriter(new OutputStreamWriter(System.out,System.getProperty("jline.WindowsTerminal.output.encoding",System.getProperty("file.encoding")))));
		        reader.addCompletor(new ArgumentCompletor(new SimpleCompletor(new String[] { "foo", "bar","baz" })));
				while ((s = reader.readLine("[jot]"+joint.getName()+":$")) != null) {
					Jot_command(s);
				}
		   }else{
		   		//stdin is from pipe	   		
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				while ((s = reader.readLine()) != null) {
					String head = "[jot]"+joint.getName()+":$";
					System.out.print(head);
					Jot_command(s);
				}
		   }
	}
	private void Jot_command(String s) throws Throwable{
		if (s.matches("^ls.*")){
			Jot_command_ls(s);
		}else if (s.equals("pac")){
			Jot_command_pac(s);
		}else if (s.equals("classes")){
			Jot_command_classes(s);
		}else if (s.matches("^cd.*")){
			joint = new Joint(s.split(" ")[1]);
			System.out.println(joint.getName());
		}else if (s.equals("show")){
			System.out.println(joint.getName());
		}else if (s.equals("cons")){
	        for (i = joint.cons().iterator(); i.hasNext();) {
	            Constructor cons = (Constructor)i.next();
	        	System.out.println(cons.toString()); 
			}
		}else if (s.equals("path")){
			System.out.println(System.getProperty("java.class.path"));
		}else if (s.equals("two2")){
			joint = new Joint("Test");
			System.out.println(joint.invoke());
		}else if (s.equals("")){
			System.out.println(joint.getName());
		}else if (s.equals("exit")){
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this._config_file_name))));
			bw.write(joint.getName(), 0, joint.getName().length());
			bw.newLine();
	        bw.close();
			System.exit(0);
		}else{	
			//method name argments
			String[] args = s.split(" "); 
        	String method_name = args[0];
			System.out.println(method_name); 
			ArrayList a = joint.search(method_name);
			if (a.size()>0){
				for (i = a.iterator(); i.hasNext();) {
		            Method m = (Method)i.next();
		            Class[] classes = m.getParameterTypes();
//				        	System.out.println(c.length); 
//				        	System.out.println(a.size()); 
		            if (classes.length == args.length - 1){
						//that means input parameter and method parameter numbers are the same
		            	List aa = Arrays.asList(classes);	
						int count=0;
						Object[] params = new Object[classes.length]; 
						for (ii = aa.iterator(); ii.hasNext();) {
				            count ++;
							Class cc = (Class)ii.next();
				        	System.out.println(cc.getName()); 
							params[count-1] = args[count];
			            }		
			        	System.out.println(joint.invoke(method_name,params)); 
			        	System.out.println(m.toString()); 
					}else{
						System.out.println("no param");
					}
		        }
			}else{
				System.out.println("NG");
			}					
			//int i = Integer.valueOf(s.split(" ")[1]).intValue();
			//Object params[] = {new Integer(i)};
			//System.out.println(joint.invoke(s.split(" ")[0],params));

		}	
	}	
	public void Jot_command_ls(String s){
		boolean is_detail = false;
		boolean is_all = false;
		int c;
		Getopt options = new Getopt("ls", s.split(" ") , "al");
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
	}
	public void Jot_command_pac(String s){
		a = Arrays.asList(Package.getPackages());
		TreeSet set = new TreeSet();
		for (i = a.iterator(); i.hasNext();) {
        	Package p = (Package)i.next();
    		set.add(p.getName()); 
        }					
		for (i = set.iterator(); i.hasNext();) {
    		System.out.println(i.next()); 
		}					
	}
	public void Jot_command_classes(String s){
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
	}

    public static void main(String args[]) {
        try {new Jot(args);}catch(Throwable t){System.out.println(t.toString());}
    }
}
class Test{
	public String two(String i){
		return ">>>"+i;
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
		Class[] argTypes = new Class[params.length];
		for(int i=0;i<params.length;i++){
			argTypes[i] = params[i].getClass();
		}		
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