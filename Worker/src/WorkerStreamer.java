import lib.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class WorkerStreamer extends Thread{

    private Worker worker;
    private Socket socket;
    private ObjectInputStream in;

    public WorkerStreamer(Socket socket, Worker worker) {
        this.worker = worker;
        this.socket = socket;
        setupReceiver();
    }

    private void setupReceiver(){
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (worker.isOnline()){
            try {
                Object message = in.readObject();
                if (message instanceof RequestToWorkerMessage) {
                    RequestToWorkerMessage rtwm = (RequestToWorkerMessage) message;
                    ArticleToSearch a = rtwm.getArticleToSearch();
                    worker.searchArticle(a.getArticle(), a.getFindStr(), a.getSearchActivityID()
                    );
                } else if (message instanceof OtherRequestMessage){
                    if (((OtherRequestMessage) message).getType().equals(MessageType.CLOSE))
                        System.exit(0);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("WorkerStreamer: Worker disconnected from the server.");
                worker.startWorker();
                return;
            }
        }
        System.out.println("WorkerStreamer: Closing...");
    }

}
