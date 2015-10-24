package main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Waypoints implements Map<Integer, Node>, Iterable {
	HashMap<Integer, Node> nodes;
	int curID;
	ArrayList<Node> checkednodes;
	public Waypoints(){
		curID = 10000;
		checkednodes = new ArrayList<Node>();
		nodes = new HashMap<Integer, Node>();
	}
	public Waypoints(String file){
		curID = 10000;
		nodes = new HashMap<Integer, Node>();
		File f = new File(file);
		try{
			BufferedReader bis = new BufferedReader(new FileReader(f));
			String curtext = bis.readLine();
			ArrayList<String> lines = new ArrayList<String>();
			while(curtext!=null){
				//System.out.println(curtext);
				lines.add(curtext.toString());
				String[] tokens = curtext.split(";");
				int curid = Integer.parseInt(tokens[0]);
				double curx = Double.parseDouble(tokens[1]);
				double cury = Double.parseDouble(tokens[2]);
				Node curNode = new Node(curx,cury,curid);
				//System.out.println(curid);
				nodes.put(curid,curNode);
				curtext = bis.readLine();
			}

			for(String n: lines){
				String[] tokens = n.split(";");
				//System.out.println("");
				//System.out.println(n);
				for(String id: tokens){
					//System.out.println(id);
				}
				Node curnode = this.get(Integer.parseInt(tokens[0]));

				//System.out.println(tokens[0]);
				String[] parents;
				try{
					if(!(tokens.length<2)){
						String st = tokens[3];
						parents = st.split(",");
					}else{
						parents = null;
					}
				}catch(Exception e){
					//System.out.println("don't have any parents");
					parents = null;
				}
				String[] children;
				
				try{
					if(!(tokens.length<3)){
						String st = tokens[4];
						children = st.split(",");
						
					}else{
						children = null;
					}
				}catch(Exception e){
					//System.out.println("don't have any children");
					children = null;
				}
				if(curnode!=null){
					if(parents!= null){
						for(String id: parents){
							//System.out.println("adding parent");
							if(!id.equals("")){
								curnode.addParent(this.get(Integer.parseInt(id)));
							}
						}
					}
					if(children!=null){
						for(String id: children){
							//System.out.println("adding children");
							if(!id.equals("")){
								Node a = this.get(Integer.parseInt(id));
								curnode.addChild(a);
							}
						}
					}
				}
			}
			bis.close();
		}catch(FileNotFoundException e){
			System.out.println("file not found");
		}catch(IOException e){
			
		}
	}
	public void recdominancecheck(Node prevprevnode,Node prevnode,Node curnode,double angle){
		//if(!checkednodes.contains(curnode)){
		checkednodes.add(curnode);
		double newangle = prevnode.dirto(curnode);
			
		//}
	}
	public boolean save(String filename){
		try {
			FileWriter writer = new FileWriter(filename);
			for(Object n1:this){
				Entry n2 = (Entry)n1;
				Node n = (Node) n2.getValue();
				writer.write(n.save()+ "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	public boolean add(int id, Node arg0){
		return (nodes.put(id,arg0)!=null);
	}
	public boolean add(Node arg0){
		curID++;
		return (nodes.put(curID,arg0)!=null);
	}
	public boolean addhierachical(Node arg0) {
		curID++;
		boolean return1 = (nodes.put(curID,arg0)!=null);
		nodes.get(arg0.ID-1).addChild(arg0);
		arg0.addParent(nodes.get(arg0.ID-1));
		return return1;
	}

	public boolean addAll(Map<? extends Integer,? extends Node> arg0) {
		nodes.putAll(arg0);
		return true;
	}

	@Override
	public void clear() {
		nodes.clear();
		
	}

	public Iterator iterator(){
		return nodes.entrySet().iterator();
	}
	public boolean contains(Object arg0) {
		return (arg0.getClass()==new Integer(0).getClass())?nodes.containsKey(arg0):nodes.containsValue(arg0);
	}


	public Node get(int index){
		return nodes.get(index);
	}
	@Override
	public boolean isEmpty() {
		return nodes.isEmpty();
	}


	/*public Node next(Node obj){
		int ind = nodes.containsValue(obj)+1;
		if(ind<nodes.size()){
			return nodes.get(ind);
		} else {
			return nodes.get(0);
		}
	}
	public Node prev(Node obj){
		int ind = nodes.indexOf(obj)-1;
		if(ind<0){
			return nodes.get(ind);
		} else {
			return null;
		}
	}
	public boolean remove(Object arg0) {
		return nodes.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0) {
		return nodes.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return nodes.retainAll(arg0);
	}*/

	@Override
	public int size() {
		return nodes.size();
	}

	/*
	public Object[] toArray() {
		return nodes.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return nodes.toArray(arg0);
	}*/

	@Override
	public boolean containsKey(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Node>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node get(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Integer> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node put(Integer arg0, Node arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Node> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Node> values() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Node remove(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
