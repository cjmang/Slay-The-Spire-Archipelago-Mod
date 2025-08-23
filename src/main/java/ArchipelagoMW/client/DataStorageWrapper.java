package ArchipelagoMW.client;

import io.github.archipelagomw.APResult;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.Event;
import io.github.archipelagomw.events.RetrievedEvent;
import io.github.archipelagomw.events.SetReplyEvent;
import io.github.archipelagomw.network.client.SetPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DataStorageWrapper implements Closeable {
    private static final Logger logger = LogManager.getLogger(DataStorageWrapper.class);
    private static final ScheduledExecutorService cleanup = Executors.newScheduledThreadPool(1);
    private final APClient client;
    private final Map<Integer, ResponseWrapper<? extends Event>> messageMap = new HashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DataStorageWrapper(APClient client)
    {
        this.client = client;
        cleanup.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            Iterator<ResponseWrapper<? extends Event>> itr = messageMap.values().iterator();
            while(itr.hasNext())
            {
                ResponseWrapper<? extends Event> response = itr.next();
                if(now - response.createdAt >= 30_000)
                {
                    response.close();
                    itr.remove();
                }
            }
        },15, 5, TimeUnit.SECONDS);
    }

    private <T extends Event> Future<T> setupFuture(APResult<Integer> result)
    {
        CompletableFuture<T> future = new CompletableFuture<>();
        if(result.getCode() != APResult.ResultCode.SUCCESS)
        {
            future.complete(null);
        }
        else {
            messageMap.put(result.getValue(), new ResponseWrapper<>(future));
        }
        return future;
    }

    private <T extends Event> void setupConsumer(APResult<Integer> result, Consumer<T> consumer)
    {
        if(result.getCode() == APResult.ResultCode.SUCCESS) {
            messageMap.put(result.getValue(), new ResponseWrapper<>(consumer));
        }
    }

    public synchronized Future<RetrievedEvent> dataStorageGet(Collection<String> keys)
    {
        APResult<Integer> result = client.dataStorageGet(keys);
        return setupFuture(result);
    }

    public synchronized Future<SetReplyEvent> dataStorageSet(SetPacket packet)
    {
        packet.want_reply = true;
        APResult<Integer> result = client.dataStorageSet(packet);
        return setupFuture(result);
    }

    public void setNoReply(SetPacket packet)
    {
        packet.want_reply = false;
        client.dataStorageSet(packet);
    }

    public synchronized void asyncDSSet(SetPacket packet, Consumer<SetReplyEvent> lambda)
    {
        packet.want_reply = true;
        APResult<Integer> result = client.dataStorageSet(packet);
        setupConsumer(result, lambda);
    }

    public synchronized void asyncDSGet(Collection<String> keys, Consumer<RetrievedEvent> lambda)
    {
        APResult<Integer> result = client.dataStorageGet(keys);
        setupConsumer(result, lambda);
    }

    @ArchipelagoEventListener
    public synchronized void handleDataStorageGetEvent(RetrievedEvent event)
    {
        if(closed.get())
        {
            return;
        }
        ResponseWrapper<Event> response = (ResponseWrapper<Event>) messageMap.remove(event.getRequestID());
        if(response != null)
        {
            response.complete(event);
        }
    }

    @ArchipelagoEventListener
    public synchronized void handleDataStorageSetEvent(SetReplyEvent event)
    {
        if(closed.get())
        {
            return;
        }
        ResponseWrapper<Event> response = (ResponseWrapper<Event>) messageMap.remove(event.getRequestID());
        if(response != null)
        {
            response.complete(event);
        }
    }


    private static class ResponseWrapper<T extends Event> implements Closeable
    {
        private final CompletableFuture<T> future;
        private final Consumer<T> consumer;
        private final long createdAt = System.currentTimeMillis();

        public ResponseWrapper(CompletableFuture<T> future) {
            this.future = future;
            this.consumer = null;
        }

        public ResponseWrapper(Consumer<T> consumer) {
            this.consumer = consumer;
            this.future = null;
        }

        public void complete(T event)
        {
            if(future != null)
            {
                future.complete(event);
            }
            else
            {
                try
                {
                    consumer.accept(event);
                }
                catch(RuntimeException ex)
                {
                    logger.info("Error while accepting datastorage interaction", ex);
                }
            }

        }

        @Override
        public void close()
        {
            if(future != null && !future.isDone())
            {
                future.cancel(false);
            }
        }
    }

    @Override
    public void close() {
        closed.set(true);
        for( ResponseWrapper<? extends Event> response : messageMap.values())
        {
            response.close();
        }
        messageMap.clear();
    }
}
