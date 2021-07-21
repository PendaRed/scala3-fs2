# FS2 pull

## It's not obvious

Well, to me it isn't.  So I did some code.

## Background notes:

From Fabio Labella

F[_] is an effect, lets face it usually an IO.
O are the emitted values
R is the returned result

So a pull can emit some elements and then terminate with a result.

The result can be used to be 'the rest'

an aside
Nothing means can't return, Unit is 'completes with no information'

If you have a stream, you can call uncons1 on it and it returns a pull.

You can use the Pull to do stateful like actions by manipulating R and recursing.

uncons1 on a stream returns a Pull that emits nothing.

"Streams can be used for two things
one is actual streaming IO (take things and transform them without accumulating too much in memory)
the other is for control flow
(do ten requests, transforms all responses)
in a kinda FRP fashion (that's what my talk is about)
now it turns out that these two use cases benefit from different monad instances
for Stream (control flow), you want a Monad in O
so you can say "do this on each emitted value" very conveniently
whereas for Pulling, you want a Monad in R, so you can say "keep recursing" very conveniently"

"using scan is actually easier than recursion in the long term (for most but not all cases)"

