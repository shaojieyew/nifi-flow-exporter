package ysj.nifi.exporter;


public abstract class Exporter {

    public void run(){
        new Thread(() -> {
            while(true){

                try {
                    export();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected abstract void export() throws Exception;
}
