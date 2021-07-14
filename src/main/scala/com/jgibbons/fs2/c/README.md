## The problem

I have a Kafka consumer, which reads messages.
I also have a control function, which has to tell that consumer to disconnect and start reading from another offset.

ie we have an FS2 stream which needs to restart another fs2 stream.

## The fs2, scala 3 solution.

To stop a consumer, we should use a signal (or defer).  Anyway, the issue is that the signal is a use once construct.

So, the command consumer should stay alive, and when it gets the rest message it has to use an FS2 topic, publishing the reset message.
Another function can set up the message consumer, with a stream listening to this topic, and on receipt it can signal that it should stop.

The topic subscriber and message consumer can then be placed into a repeatable stream, so that after death they restart.

ie, not sure how to draw this with text, but a bit like below:
<pre>
Create an fs2 topic {
    Kafka reset consumer gets the command message, and publishes to FS2 topic
    concurrently it runs a stream
        Stream with a signal (
          FS2 topic consumer sets the signal to true
          Kafka message consumer consumes messages until signalled to die
        ).repeat
}
</pre>
Anyway, the real code is actually more readable - see MainNestedStreams for the main.

The output looks like:
<pre>
io-compute-3 Message offset 1
io-compute-1 Message offset 2
io-compute-0 Message offset 3
io-compute-2 Message offset 4
io-compute-2 Interrupting to offset 223!
io-compute-1 Message offset 5
io-compute-0 Interrupting!, new offset is 223
io-compute-0 Message offset 224
io-compute-3 Message offset 225
io-compute-1 Message offset 226
io-compute-0 Message offset 227
io-compute-0 Interrupting to offset 352!
io-compute-1 Interrupting!, new offset is 352
io-compute-1 Message offset 353
io-compute-3 Message offset 354
...
</pre>