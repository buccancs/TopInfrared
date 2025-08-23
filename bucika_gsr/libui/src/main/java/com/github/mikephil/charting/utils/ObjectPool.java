package com.github.mikephil.charting.utils;

import java.util.List;

public class ObjectPool<T extends ObjectPool.Poolable> {

    private static int ids = 0;

    private int poolId;
    private int desiredCapacity;
    private Object[] objects;
    private int objectsPointer;
    private T modelObject;
    private float replenishPercentage;

    public int getPoolId(){
        return poolId;
    }

    public static synchronized ObjectPool create(int withCapacity, Poolable object){
        ObjectPool result = new ObjectPool(withCapacity, object);
        result.poolId = ids;
        ids++;

        return result;
    }

    private ObjectPool(int withCapacity, T object){
        if(withCapacity <= 0){
            throw new IllegalArgumentException("Object Pool must be instantiated with a capacity greater than 0!");
        }
        this.desiredCapacity = withCapacity;
        this.objects = new Object[this.desiredCapacity];
        this.objectsPointer = 0;
        this.modelObject = object;
        this.replenishPercentage = 1.0f;
        this.refillPool();
    }

    public void setReplenishPercentage(float percentage){
        float p = percentage;
        if(p > 1){
            p = 1;
        }
        else if(p < 0f){
            p = 0f;
        }
        this.replenishPercentage = p;
    }

    public float getReplenishPercentage(){
        return replenishPercentage;
    }

    private void refillPool(){
        this.refillPool(this.replenishPercentage);
    }

    private void refillPool(float percentage){
        int portionOfCapacity = (int) (desiredCapacity * percentage);

        if(portionOfCapacity < 1){
            portionOfCapacity = 1;
        }else if(portionOfCapacity > desiredCapacity){
            portionOfCapacity = desiredCapacity;
        }

        for(int i = 0 ; i < portionOfCapacity ; i++){
            this.objects[i] = modelObject.instantiate();
        }
        objectsPointer = portionOfCapacity - 1;
    }

    public synchronized T get(){

        if(this.objectsPointer == -1 && this.replenishPercentage > 0.0f){
            this.refillPool();
        }

        T result = (T)objects[this.objectsPointer];
        result.currentOwnerId = Poolable.NO_OWNER;
        this.objectsPointer--;

        return result;
    }

    public synchronized void recycle(T object){
        if(object.currentOwnerId != Poolable.NO_OWNER){
            if(object.currentOwnerId == this.poolId){
                throw new IllegalArgumentException("The object passed is already stored in this pool!");
            }else {
                throw new IllegalArgumentException("The object to recycle already belongs to poolId " + object.currentOwnerId + ".  Object cannot belong to two different pool instances simultaneously!");
            }
        }

        this.objectsPointer++;
        if(this.objectsPointer >= objects.length){
            this.resizePool();
        }

        object.currentOwnerId = this.poolId;
        objects[this.objectsPointer] = object;

    }

    public synchronized void recycle(List<T> objects){
        while(objects.size() + this.objectsPointer + 1 > this.desiredCapacity){
            this.resizePool();
        }
        final int objectsListSize = objects.size();

        for(int i = 0 ; i < objectsListSize ; i++){
            T object = objects.get(i);
            if(object.currentOwnerId != Poolable.NO_OWNER){
                if(object.currentOwnerId == this.poolId){
                    throw new IllegalArgumentException("The object passed is already stored in this pool!");
                }else {
                    throw new IllegalArgumentException("The object to recycle already belongs to poolId " + object.currentOwnerId + ".  Object cannot belong to two different pool instances simultaneously!");
                }
            }
            object.currentOwnerId = this.poolId;
            this.objects[this.objectsPointer + 1 + i] = object;
        }
        this.objectsPointer += objectsListSize;
    }

    private void resizePool() {
        final int oldCapacity = this.desiredCapacity;
        this.desiredCapacity *= 2;
        Object[] temp = new Object[this.desiredCapacity];
        for(int i = 0 ; i < oldCapacity ; i++){
            temp[i] = this.objects[i];
        }
        this.objects = temp;
    }

    public int getPoolCapacity(){
        return this.objects.length;
    }

    public int getPoolCount(){
        return this.objectsPointer + 1;
    }

    public static abstract class Poolable{

        public static int NO_OWNER = -1;
        int currentOwnerId = NO_OWNER;

        protected abstract Poolable instantiate();

    }
}