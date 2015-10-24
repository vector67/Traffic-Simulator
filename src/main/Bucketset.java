package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Bucketset implements Collection<Bucket> {
	Bucket[][] buckets;
	int width,height;
	int rwidth,rheight;
	
	public Bucketset(int x, int y){
		this(x,y,0,0);
	}
	
	public Bucketset(int x, int y,int width1,int height1){
		width = x;
		height = y;
		rwidth = width1;
		rheight = height1;
		buckets = new Bucket[x][y];
	}
	
	@Override
	public boolean add(Bucket arg0) {
		for(int y = 0;y<height;y++)
			for(int x= 0;x<width;x++)
				if(buckets[x][y]==null){
					buckets [x][y] = arg0;
					return true;
				}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Bucket> arg0) {
		for(Bucket b: arg0){
			if(!add(b)){
				return false;
			}
		}
		return true;
	}

	@Override
	public void clear() {
		for(int x= 0;x<width;x++)
			for(int y = 0;y<height;y++)
				buckets[x][y]=null;
		
	}
	
	public Bucket[] surroundBuckets(Bucket b){
		Bucket[] buckets2 = new Bucket[9];
		int x = (int)(b.x-b.width/2)/b.width;
		int y = (int)(b.y-b.height/2)/b.height;
		if(x!=width-1){
			if(y!=0){
				buckets2[2] = buckets[x+1][y-1];
			}else{
				buckets2[2] = null;
			}
			buckets2[3] = buckets[x+1][y];
			if(y!=height-1){
				buckets2[4] = buckets[x+1][y+1];
			}else{
				buckets2[4] = null;
			}
		}else{
			buckets2[2] = null;
			buckets2[3] = null;
			buckets2[4] = null;
		}
		if(y!=height-1){
			if(x!=0){
				buckets2[6] = buckets[x-1][y+1];
			}else{
				buckets2[6] = null;
			}
	 		buckets2[5] = buckets[x][y+1];

		}else{
			buckets2[6] = null;
			buckets2[5] = null;
		}
		if(x!=0){
			if(y!=0){
				buckets2[0] = buckets[x-1][y-1];
			} else{
				buckets2[0] = null;
			}
			buckets2[7] = buckets[x-1][y];
		} else{
			buckets2[0] = null;
			buckets2[7] = null;
		}
		if(y!=0){
			buckets2[1] = buckets[x][y-1];
		}else{
			buckets2[1] = null;
		}
		// 0 1 2
		// 7 8 3
		// 6 5 4
		buckets2[8] = b;
		return buckets2;
		
	}
	
	@Override
	public boolean contains(Object arg0) {
		try{
			Bucket b = (Bucket) arg0;
			int x = (int) (b.x/b.width);
			int y = (int) (b.y/b.height);
			if(buckets[x][y]==b){
				return true;
			}else{
				throw new Exception();
			}
		}catch(Exception e){
			//e.printStackTrace();
			for(int x= 0;x<width;x++)
				for(int y = 0;y<height;y++)
					if(buckets[x][y]==arg0)
						return true;
		}
		
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for(Object b: arg0){
			if(!contains(b)){
				return false;
			}
		}
		return true;
	}
	
	/*
	 * 0 - up
	 * 1 - right
	 * 2 - down
	 * 3 - left
	 */
	public Bucket next(int direction,Bucket b1) throws ArrayIndexOutOfBoundsException {
		double x = (b1.x-b1.width/2)/b1.width;
		double y = (b1.y-b1.height/2)/b1.height;
		Bucket b;
		try{
			/*System.out.println(((direction%2==1)?(direction-1)*-1+1:0));
			System.out.println(((direction%2==0)?direction-1:0));
			System.out.println(x);
			System.out.println(y);
			System.out.println(b1.x);
			System.out.println(b1.width);*/
			b = buckets[(int)x+((direction%2==1)?(direction-1)*-1+1:0)][(int)y+((direction%2==0)?direction-1:0)];
			return b;
		}catch (ArrayIndexOutOfBoundsException e){
			return null;
		}
	
	}
	
	public Bucket get(int index){
		return buckets[(int)Math.floor((double)index/(double)width)][index%width];
	}
	
	public Bucket get(int indexx, int indexy){
		return buckets[indexx][indexy];
	}
	
	public Bucket getBucketIn(int x,int y){
		if(rwidth!=0){
			int divisorx = (int) rwidth/width;
			int divisory = (int) rheight/height;
			return buckets[(int)Math.floor(x/divisorx)][(int)Math.floor(y/divisory)];
		}else{
			for(int x1= 0;x1<width;x1++)
				for(int y1 = 0;y1<height;y1++)
					if(buckets[x1][y1].isPointIn(x,y)){
						return buckets[x1][y1];
					}
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		for(int x= 0;x<width;x++)
			for(int y = 0;y<height;y++)
				if(buckets[x][y]!=null)
					return false;
		return true;
	}

	@Override
	public Iterator<Bucket> iterator() {
		
		return new Itr();
	}

	/*public Bucket next(Bucket obj){
		int ind = buckets.indexOf(obj)+1;
		if(ind<buckets.size()){
			return buckets.get(ind);
		} else {
			return buckets.get(0);
		}
	}
	public Bucket prev(Bucket obj){
		int ind = buckets.indexOf(obj)-1;
		if(ind<0){
			return buckets.get(ind);
		} else {
			return null;
		}
	}*/

	@Override

	public boolean remove(Object arg0) {
		for(int x= 0;x<width;x++)
			for(int y = 0;y<height;y++)
				if(buckets[x][y]==arg0){
					buckets[x][y]=null;
					return true;
				}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		for(Object o: arg0)
			if(!remove(o))
				return false;
		return true;
	}

	@Override
	public Object[] toArray() {
		return buckets[0];
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return null;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	private class Itr implements Iterator<Bucket> {
		 int cursorx;       // index of next element to return
		 int cursory;       // index of next element to return
		 int lastRetx = -1; // index of last element returned; -1 if no such
		 int lastRety = -1; // index of last element returned; -1 if no such
         //int expectedModCount = modCount;
 
         public boolean hasNext() {
             return (cursorx != buckets.length)&&(cursory!=buckets[0].length);
         }
 
         @SuppressWarnings("unchecked")
         public Bucket next() {
             //checkForComodification();
             int x = cursorx;
             int y = cursory;
             if (!hasNext())
                 throw new NoSuchElementException();
             Bucket[][] elementData = buckets;
             cursorx = cursorx + 1;
             if(cursorx>elementData.length-1){
            	 cursory = cursory+1;
            	 cursorx = 0;
             }
             return (Bucket) elementData[lastRetx = x][lastRety = y];
         }
 
         public void remove() {
             if (lastRetx < 0)
                 throw new IllegalStateException();
             //checkForComodification();
 
             try {
                 Bucketset.this.remove(lastRetx+lastRety);
                 cursorx = lastRetx;
                 cursory = lastRety;
                 lastRetx = -1;
                 lastRety = -1;
                 //expectedModCount = modCount;
             } catch (IndexOutOfBoundsException ex) {
                 throw new ConcurrentModificationException();
             }
         }
 
         /*final void checkForComodification() {
             if (modCount != expectedModCount)
                 throw new ConcurrentModificationException();
         }*/
 	}
}
