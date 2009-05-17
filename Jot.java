
import java.io.*;
import java.util.*;
//import java.lang.reflect.*;
//import gnu.getopt.Getopt;
//import jline.*;
class Jot {
	Joint _joint;
	static final String _config_file_name = ".config";
	public Jot(String in_args[]) throws Throwable{
		//config file create
		String filename = this._config_file_name;
		if(!new File(filename).exists()){new File(filename).createNewFile();}
		//java env
		filename = this._config_file_name;
		if (new File(filename).exists()){_joint = new Joint(new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)))).readLine());}else{_joint = new Joint("");}
		//java option
		int c; 
		String[] args = in_args;
		gnu.getopt.Getopt options = new gnu.getopt.Getopt("java Jot", args , "s");
        while ( (c = options.getopt()) != -1) {if (c == 's'){System.out.println("stdin");}}
		//
		args = in_args;
		if (args.length>0){
			//command line mode, operate one command then exit
			//join args
	    	StringBuffer buf = new StringBuffer();for (String sss: args) {if (buf.length()>0) {buf.append(" ");}buf.append(sss);}
			this.Jot_command(buf.toString());
			System.exit(0);
		}

		//main loop for stdin
		String s="";
    	if (System.console() != null) {	
    		//stdin is from keyboard
			jline.ConsoleReader reader = new jline.ConsoleReader(System.in,new PrintWriter(new OutputStreamWriter(
					System.out,System.getProperty("jline.WindowsTerminal.output.encoding",System.getProperty("file.encoding")))));
			while ((s = reader.readLine("[jot]"+_joint.getName()+":$")) != null) {
		        reader.addCompletor(new jline.ArgumentCompletor(new jline.SimpleCompletor(new String[] { "foo", "bar","baz" })));
				this.Jot_command(s);
			}
	   }else{
	   		//stdin is from pipe	   		
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			while ((s = reader.readLine()) != null) {
				//String head = "[jot]"+_joint.getName()+":$";
				//System.out.print(head);
				this.Jot_command(s);
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
			_joint = new Joint(s.split(" ")[1]);
			System.out.println(_joint.getName());
		}else if (s.equals("show")){
			System.out.println(_joint.getName());
		}else if (s.equals("cons")){
	        for (Iterator<java.lang.reflect.Constructor> i = _joint.cons().iterator(); i.hasNext();) {
	            java.lang.reflect.Constructor cons = i.next();
	        	System.out.println(cons.toString()); 
			}
		}else if (s.equals("objects")){
		}else if (s.equals("pwd")){
			System.out.println(_joint.getName());
		}else if (s.equals("path")){
			System.out.println(System.getProperty("java.class.path"));
		}else if (s.equals("two2")){
			_joint = new Joint("Test");
			System.out.println(_joint.invoke());
		}else if (s.equals("")){
			System.out.println(_joint.getName());
		}else if (s.equals("exit")){
			String filename = this._config_file_name;
			String ss =	_joint.getName();		
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename))));
			bw.write(ss, 0, ss.length());
			bw.newLine();
	        bw.close();
			System.exit(0);
		}else if (s.matches("^call.*")){
			Jot_command_call(s);
		}else{	
			//method name argments
			Jot_command_call(s);
		}	
	}	
	public void Jot_command_call(String s)throws Throwable{
		String[] args = s.split(" "); 
    	String method_name = args[0];
		ArrayList<java.lang.reflect.Method> a = _joint.search(method_name);
		if (a.size()>0){
			for (Iterator<java.lang.reflect.Method> i = a.iterator(); i.hasNext();) {
	            java.lang.reflect.Method m = i.next();
	            Class[] classes = m.getParameterTypes();
	            if (classes.length ==0){
		        	System.out.println(_joint.invoke(method_name)); 
				}else{	            	
		            if (classes.length == args.length - 1){
						//that means input parameter and method parameter numbers are the same
		            	List<Class> aa = Arrays.asList(classes);	
						int count=0;
						Object[] params = new Object[classes.length]; 
						for (Iterator<Class>ii = aa.iterator(); ii.hasNext();) {
				            count ++;
							Class cc = ii.next();
				        	//System.out.println(cc.getName()); 
							params[count-1] = args[count];
			            }		
			        	System.out.println(_joint.invoke(method_name,params)); 
					}else{
						System.out.println("no param");
					}
	            }

	        }
		}else{
			System.out.println("NG");
		}					
		//int i = Integer.valueOf(s.split(" ")[1]).intValue();
		//Object params[] = {new Integer(i)};
		//System.out.println(_joint.invoke(s.split(" ")[0],params));

	}
	public void Jot_command_ls(String s){
		boolean is_detail = false;
		boolean is_all = false;
		int c;
		gnu.getopt.Getopt options = new gnu.getopt.Getopt("ls", s.split(" ") , "al");
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

        List<java.lang.reflect.Method> a;
        if (is_all){
			a = Arrays.asList(_joint.getClassObject().getMethods());												
		}else{
			a = Arrays.asList(_joint.getClassObject().getDeclaredMethods());										
		}
		for (Iterator<java.lang.reflect.Method>i = a.iterator(); i.hasNext();) {
            java.lang.reflect.Method m = (java.lang.reflect.Method)i.next();
            if (is_detail){
	        	System.out.println(m.toString()); 
			}else{
	        	System.out.println(m.getName()); 
			}
        }
	}
	public void Jot_command_pac(String s){
		List<Package> a = Arrays.asList(Package.getPackages());
		TreeSet set = new TreeSet();
		for (Iterator<Package>i = a.iterator(); i.hasNext();) {
        	Package p = i.next();
    		set.add(p.getName()); 
        }					
		for (Iterator<String>i = set.iterator(); i.hasNext();) {
    		System.out.println(i.next()); 
		}					
	}
	public void Jot_command_classes(String s){
		List<String> a = Arrays.asList(System.getProperty("java.class.path").split(System.getProperty("path.separator")));
        for (Iterator<String> i = a.iterator(); i.hasNext();) {
            String ss = i.next();
			File f = new File(ss);
			if (f.isDirectory()){
				List<String> aa  = Arrays.asList(f.list());
		        for (Iterator<String> ii = aa.iterator(); ii.hasNext();) {
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
	public String fee(){
		return "2222";
	}
}
class Joint{
	Class _class;
	Object _object;
	public Class getClassObject(){
		return _class;
	}
	public Joint(String s,int i){}
	public ArrayList<java.lang.reflect.Method>  search(String method_name)throws Throwable{
		List<java.lang.reflect.Method> a = Arrays.asList(_class.getDeclaredMethods());		
		ArrayList<java.lang.reflect.Method> return_methods= new ArrayList();
		for (Iterator<java.lang.reflect.Method>i = a.iterator(); i.hasNext();) {
            java.lang.reflect.Method m = i.next();
			if (m.getName().equals(method_name)){return_methods.add(m);}
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
	public Object invoke(String method_name){
		try{
			Class[] argTypes=null;Object params[]=null;
			return _class.getMethod(method_name,argTypes).invoke(_object,params); 
		 }catch(Throwable th){
            System.out.println("eeeeh"+th.toString());
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
	public List<java.lang.reflect.Constructor> cons(){
		return java.util.Arrays.asList(_class.getConstructors());		
	}

}