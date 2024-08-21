import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static final int QUEUE_CAPACITY = 100;

    private static final int NUMBER_OF_TEXTS = 10000;
    private static final int TEXT_LENGTH = 100000;

    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    public static void main(String[] args) throws InterruptedException {
      Thread generatorThread = new Thread(() -> {


              try {
                  for (int i=0; i < NUMBER_OF_TEXTS ; i++ ) {
                      String text = generateText();
                      queueA.put(text);
                      queueB.put(text);
                      queueC.put(text);
                  }
                  queueA.put("END");
                  queueB.put("END");
                  queueC.put("END");
              } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
              }

      });

        CountDownLatch latch = new CountDownLatch(3);

        Thread threadA = createAnalyzerThread(queueA,'a', latch);
        Thread threadB = createAnalyzerThread(queueB,'b', latch);
        Thread threadC = createAnalyzerThread(queueC,'c', latch);

        generatorThread.start();
        threadA.start();
        threadB.start();
        threadC.start();

        latch.await();

        System.out.println("Анализ завершён.");
    }

    private static Thread createAnalyzerThread(BlockingQueue<String> queue, char targetChar, CountDownLatch latch) {
        return new Thread(() -> {
            String maxText = null;
            int maxCount = 0;

            try {
                while (true){
                    String text=queue.take();
                    if ("END".equals(text)){
                        break;
                    }
                    int count = countChar(text,targetChar);
                    if (count>maxCount){
                     maxCount=count;
                     maxText=text;

                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
                System.out.printf("Максимальное количество символов '%c': %d\n", targetChar, maxCount);
            }

        });
    }

    private static String generateText() {
        StringBuilder text = new StringBuilder(TEXT_LENGTH);
        for (int i = 0; i < TEXT_LENGTH; i++) {
            text.append((char) ('a' + ThreadLocalRandom.current().nextInt(3)));
        }
        return text.toString();
    }

    private static int countChar(String text, char targetChar) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == targetChar) {
                count++;
            }
        }
        return count;
    }

}