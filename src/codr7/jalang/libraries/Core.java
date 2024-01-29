package codr7.jalang.libraries;

import codr7.jalang.*;
import codr7.jalang.errors.EmitError;
import codr7.jalang.errors.EvaluationError;
import codr7.jalang.forms.*;
import codr7.jalang.operations.Set;
import codr7.jalang.operations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Core extends Library {
  public static final Type<Object> anyType = new Type<>("Any");
  public static final BitType bitType = new BitType("Bit");
  public static final Value<Boolean> T = new Value<>(bitType, true);
  public static final Value<Boolean> F = new Value<>(bitType, false);
  public static final CharacterType characterType = new CharacterType("Character");
  public static final CollectionType collectionType = new CollectionType("Collection");
  public static final ComparableType comparableType = new ComparableType("Comparable");
  public static final DotType dotType = new DotType("Dot");
  public static final Type<Function> functionType = new FunctionType("Function");
  public static final IndexedCollectionType indexedCollectionType = new IndexedCollectionType("IndexedCollection");
  public static final IntegerType integerType = new IntegerType("Integer");
  public static final IteratorType iteratorType = new IteratorType("Iterator");
  public static final Iterator2Type iterator2Type = new Iterator2Type("Iterator2");
  public static final ListType listType = new ListType("List");
  public static final MacroType macroType = new MacroType("Macro");
  public static final Type<MacroReference> macroReferenceType = new Type<>("MacroReference");
  public static final MapType mapType = new MapType("Map");
  public static final MetaType metaType = new MetaType("Meta");
  public static final NoneType noneType = new NoneType("None");
  public static final Value<Object> NONE = new Value<>(noneType, null);
  public static final PairType pairType = new PairType("Pair");
  public static final Type<Path> pathType = new Type<>("Path");
  public static final RegisterType registerType = new RegisterType("Register");
  public static final SequenceType sequenceType = new SequenceType("Sequence");
  public static final SetType setType = new SetType("Set");
  public static final StringType stringType = new StringType("String");
  public static final SymbolType symbolType = new SymbolType("Symbol");
  public static final TimeType timeType = new TimeType("Time");
  public static final RegisterType variableType = new RegisterType("Variable");
  public static final VectorType vectorType = new VectorType("Vector");

  public Core() {
    super("core", null);
    bindType(anyType);
    bindType(bitType);
    bindType(characterType);
    bindType(collectionType);
    bindType(comparableType);
    bindType(dotType);
    bindType(functionType);
    bindType(indexedCollectionType);
    bindType(integerType);
    bindType(iteratorType);
    bindType(iterator2Type);
    bindType(listType);
    bindType(macroType);
    bindType(macroReferenceType);
    bindType(mapType);
    bindType(metaType);
    bindType(noneType);
    bindType(pairType);
    bindType(pathType);
    bindType(registerType);
    bindType(sequenceType);
    bindType(setType);
    bindType(stringType);
    bindType(symbolType);
    bindType(timeType);
    bindType(vectorType);

    bind("_", NONE);
    bind("T", T);
    bind("F", F);

    bindFunction("=",
        new String[]{"value1", "value2"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var value1 = vm.get(rParameters[0]);
          var result = true;

          for (int i = 1; i < rParameters.length; i++) {
            if (!vm.get(rParameters[i]).equals(value1)) {
              result = false;
              break;
            }
          }

          vm.set(rResult, new Value<>(bitType, result));
        });

    bindFunction("<",
        new String[]{"value1", "value2"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          var value1 = vm.get(rParameters[0]);
          var type = (ComparableTrait) value1.type();
          var result = true;

          for (var i = 1; i < rParameters.length; i++) {
            final var v = vm.get(rParameters[i]);

            if (v.type() != value1.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", value1.type(), v.type());
            }

            if (type.compare(value1, v) != Order.LessThan) {
              result = false;
              break;
            }

            value1 = v;
          }

          vm.set(rResult, new Value<>(bitType, result));
        });

    bindFunction(">",
        new String[]{"value1", "value2"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          var value1 = vm.get(rParameters[0]);
          var type = (ComparableTrait) value1.type();
          var result = true;

          for (var i = 1; i < rParameters.length; i++) {
            final var v = vm.get(rParameters[i]);

            if (v.type() != value1.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", value1.type(), v.type());
            }

            if (type.compare(value1, v) != Order.GreaterThan) {
              result = false;
              break;
            }

            value1 = v;
          }

          vm.set(rResult, new Value<>(bitType, result));
        });

    bindFunction("+",
        new String[]{"value1", "value2"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          int result = 0;

          for (int rParameter : rParameters) {
            result += vm.get(rParameter).as(integerType);
          }

          vm.set(rResult, new Value<>(integerType, result));
        });

    bindFunction("-",
        new String[]{"value1"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          int result = vm.get(rParameters[0]).as(integerType);

          if (rParameters.length == 1) {
            result = -result;
          } else {
            for (var i = 1; i < rParameters.length; i++) {
              result -= vm.get(rParameters[i]).as(integerType);
            }
          }

          vm.set(rResult, new Value<>(integerType, result));
        });

    bindMacro("=0", 1,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          vm.emit(new EqualsZero(rResult, rResult));
        });

    bindMacro("+1", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var a = arguments[0];
          int rValue;

          if (a instanceof IdForm) {
            final var v = namespace.find(((IdForm) a).name());

            if (v.type() != registerType) {
              throw new EmitError(location, "Invalid target: %s", v.toString());
            }

            rValue = v.as(registerType);
          } else if (a instanceof LiteralForm) {
            rValue = vm.allocateRegister();
            vm.emit(new Set(rValue, ((LiteralForm) a).value()));
          } else {
            throw new EmitError(location, "Invalid target: %s", a.toString());
          }

          vm.emit(new Increment(rValue, rResult));
        });

    bindMacro("-1", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var a = arguments[0];
          int rValue;

          if (a instanceof IdForm) {
            final var v = namespace.find(((IdForm) a).name());

            if (v.type() != registerType) {
              throw new EmitError(location, "Invalid target: %s", v.toString());
            }

            rValue = v.as(registerType);
          } else if (a instanceof LiteralForm) {
            rValue = vm.allocateRegister();
            vm.emit(new Set(rValue, ((LiteralForm) a).value()));
          } else {
            throw new EmitError(location, "Invalid target: %s", a.toString());
          }

          vm.emit(new Decrement(rValue, rResult));
        });

    bindMacro("and", 2,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          final var andPcs = new ArrayList<Integer>();

          for (int i = 1; i < arguments.length; i++) {
            andPcs.add(vm.emit());
            arguments[i].emit(vm, namespace, rResult);
          }

          for (final var pc : andPcs) {
            vm.emit(pc, new If(rResult, vm.emitPc()));
          }
        });

    bindFunction("append",
        new String[]{"input1"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var result = new ArrayList<Value<?>>();

          for (final var p : rParameters) {
            final var input = vm.get(p);
            final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input);

            while (iterator.hasNext()) {
              result.add(iterator.next());
            }
          }

          vm.set(rResult, new Value<>(iteratorType, result.iterator()));
        });

    bindFunction("apply",
        new String[]{"target"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var target = vm.get(rParameters[0]);

          if (!(target.type() instanceof CallableTrait)) {
            throw new EvaluationError(location, "Target is not callable: %s.", target);
          }

          final var lp = vm.get(rParameters[rParameters.length - 1]);

          if (!(lp.type() instanceof SequenceTrait<?>)) {
            throw new EvaluationError(location, "Final parameter should be a sequence: %s.", lp);
          }

          final var lpi = ((SequenceTrait<Value<?>>) lp.type()).iterator(lp);
          final var ps = new TreeMap<Integer, Value<?>>();

          while (lpi.hasNext()) {
            final var v = lpi.next();
            final var r = vm.allocateRegister();
            ps.put(r, v);
          }

          vm.reallocateRegisters();
          final var rps = new int[rParameters.length - 2 + ps.size()];
          int i = 0;

          while (i < rParameters.length - 2) {
            rps[i] = rParameters[i + 1];
            i++;
          }

          for (final var e : ps.entrySet()) {
            final var rv = e.getKey();
            rps[i] = rv;
            vm.set(rv, e.getValue());
            i++;
          }

          ((CallableTrait) target.type()).call(target, vm, namespace, location, rps, rResult);
        });

    bindMacro("benchmark", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var rRepetitions = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rRepetitions);
          vm.emit(new Benchmark(rRepetitions, rResult));

          for (int i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, namespace, rRepetitions);
          }

          vm.emit(Stop.instance);
        });

    bindFunction("call",
        new String[]{"target"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var target = vm.get(rParameters[0]);

          if (!(target.type() instanceof CallableTrait)) {
            throw new EvaluationError(location, "Target is not callable: %s.", target);
          }

          final var rps = new int[rParameters.length - 1];

          System.arraycopy(rParameters, 1, rps, 0, rps.length);

          ((CallableTrait) target.type()).call(target, vm, namespace, location, rps, rResult);
        });

    bindMacro("check", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var rExpected = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rExpected);
          final var rActual = vm.allocateRegister();
          vm.emit(new Check(rExpected, rActual, location));
          final var bodyNamespace = new Namespace(namespace);

          for (var i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, bodyNamespace, rActual);
          }

          vm.emit(Stop.instance);
        });

    bindMacro("define", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var nameForm = arguments[0];

          if (!(nameForm instanceof IdForm)) {
            throw new EmitError(nameForm.location(), "Expected identifier: %s.", nameForm);
          }

          final var name = ((IdForm) nameForm).name();
          final var rValue = vm.allocateRegister();
          vm.evaluate(arguments[1], namespace, rValue);
          namespace.bind(name, new Value<>(variableType, rValue));
        });

    bindFunction("digit",
        new String[]{"value"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var c = vm.get(rParameters[0]).as(characterType);
          final var result = Character.isDigit(c) ? Character.digit(c, 10) : -1;
          vm.set(rResult, new Value<>(integerType, result));
        });

    bindFunction("digit?",
        new String[]{"value"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var c = vm.get(rParameters[0]).as(characterType);
          vm.set(rResult, new Value<>(bitType, Character.isDigit(c)));
        });

    bindMacro("do", 0,
        (vm, namespace, location, arguments, rResult) -> {
          for (final var a : arguments) {
            a.emit(vm, namespace, rResult);
          }
        });

    bindFunction("enumerate",
        new String[]{"input"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var input = vm.get(rParameters[0]);
          final var iterator = ((SequenceTrait<?>) input.type()).iterator(input);
          final var output = new ArrayList<Value<?>>();

          for (int i = (rParameters.length == 1) ? 0 : vm.get(rParameters[1]).as(integerType);
               iterator.hasNext();
               i++) {
            final var v = iterator.next();
            output.add(new Value<>(pairType, new Pair(new Value<>(integerType, i), (Value<?>) v)));
          }

          vm.set(rResult, new Value<>(iteratorType, output.iterator()));
        });

    bindMacro("find", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var rPredicate = vm.allocateRegister();
          final var predicateForm = arguments[0];
          predicateForm.emit(vm, namespace, rPredicate);

          final var rIterator = vm.allocateRegister();
          final var inputForm = arguments[1];
          inputForm.emit(vm, namespace, rIterator);
          vm.emit(new GetIterator(rIterator, rIterator, inputForm.location()));
          vm.emit(new Set(rResult, new Value<>(noneType, null)));

          final var rIndex = vm.allocateRegister();
          vm.emit(new Set(rIndex, new Value<>(integerType, 0)));

          final var iteratePc = vm.emit();
          final var rValue = vm.allocateRegister();
          final var rPredicateResult = vm.allocateRegister();
          vm.emit(new CallIndirect(location, rPredicate, new int[]{rValue}, rPredicateResult));
          final var ifPc = vm.emit();
          vm.emit(new MakePair(rValue, rIndex, rResult));
          final var exitPc = vm.emit();
          vm.emit(ifPc, new If(rPredicateResult, vm.emitPc()));
          vm.emit(new Increment(rIndex, rIndex));
          vm.emit(new Goto(iteratePc));
          vm.emit(iteratePc, new Iterate(rIterator, rValue, vm.emitPc()));
          vm.emit(exitPc, new Goto(vm.emitPc()));
        });

    bindMacro("filter", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var rPredicate = vm.allocateRegister();
          final var predicateForm = arguments[0];
          predicateForm.emit(vm, namespace, rPredicate);

          final var rIterator = vm.allocateRegister();
          final var inputForm = arguments[1];
          inputForm.emit(vm, namespace, rIterator);
          vm.emit(new GetIterator(rIterator, rIterator, inputForm.location()));
          vm.emit(new MakeVector(rResult));

          final var rIndex = vm.allocateRegister();
          vm.emit(new Set(rIndex, new Value<>(integerType, 0)));

          final var iteratePc = vm.emit();
          final var rValue = vm.allocateRegister();
          final var rPredicateResult = vm.allocateRegister();
          vm.emit(new CallIndirect(location, rPredicate, new int[]{rValue}, rPredicateResult));
          final var ifPc = vm.emit();
          vm.emit(new MakePair(rValue, rIndex, rValue));
          vm.emit(new Push(rResult, rValue, rResult));
          vm.emit(ifPc, new If(rPredicateResult, vm.emitPc()));
          vm.emit(new Increment(rIndex, rIndex));
          vm.emit(new Goto(iteratePc));
          vm.emit(iteratePc, new Iterate(rIterator, rValue, vm.emitPc()));
        });

    bindMacro("for", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var varForm = arguments[0];

          if (!(varForm instanceof IdForm)) {
            throw new EmitError(varForm.location(), "Invalid for variable: %s.", varForm);
          }

          final var rIterator = vm.allocateRegister();
          arguments[1].emit(vm, namespace, rIterator);
          vm.emit(new GetIterator(rIterator, rIterator, location));
          final var iteratePc = vm.emit();
          final var rValue = vm.allocateRegister();
          namespace.bind(((IdForm) varForm).name(),
              new Value<>(registerType, rValue));

          for (int i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, namespace, rResult);
          }

          vm.emit(new Goto(iteratePc));
          vm.emit(iteratePc, new Iterate(rIterator, rValue, vm.emitPc()));
        });

    bindMacro("function", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var as = new ArrayDeque<Form>();
          Collections.addAll(as, arguments);
          var name = "";

          if (arguments[0] instanceof IdForm) {
            name = ((IdForm) as.removeFirst()).name();
          }

          var psForm = as.removeFirst();

          if (psForm instanceof PairForm p) {
            var tnf = p.right();
            var tv = namespace.find(((IdForm) tnf).name());

            if (tv == null) {
              throw new EmitError(tnf.location(), "Type not found: %s.", tnf);
            }

            psForm = p.left();
          }

          if (!(psForm instanceof VectorForm)) {
            throw new EmitError(psForm.location(), "Invalid parameter specification: %s.", psForm);
          }

          final var ps = Arrays.stream(((VectorForm) psForm).body()).map((f) -> {
            var pn = "";

            if (f instanceof IdForm) {
              pn = ((IdForm) f).name();
            } else {
              throw new EmitError(f.location(), "Invalid parameter: %s.", f);
            }

            return new Function.Parameter(pn, vm.allocateRegister());
          }).toArray(Function.Parameter[]::new);

          final var skipPc = vm.emit();
          final var startPc = vm.emitPc();

          final var function = new Function(name, ps,
              (_function, _vm, _location, _namespace, _parameters, _result) -> {
                _vm.pushCall(new Value<>(functionType, _function), _location, startPc, _result);

                for (var i = 0; i < _parameters.length; i++) {
                  vm.set(ps[i].rValue(), vm.get(_parameters[i]));
                }
              });

          final var value = new Value<>(functionType, function);

          if (!name.isEmpty()) {
            namespace.bind(name, new Value<>(functionType, function));
          }

          final var bodyNamespace = new Namespace(namespace);

          for (final Function.Parameter p : ps) {
            bodyNamespace.bind(p.name(), new Value<>(registerType, p.rValue()));
          }

          for (final var f : as) {
            f.emit(vm, bodyNamespace, rResult);
          }

          vm.emit(new Return(rResult));
          vm.emit(skipPc, new Goto(vm.emitPc()));

          if (name.isEmpty()) {
            vm.emit(new Set(rResult, value));
          }
        });

    alias("^", "function");

    bindMacro("if", 2,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          final var ifPc = vm.emit();
          arguments[1].emit(vm, namespace, rResult);
          final var skipPc = (arguments.length > 2) ? vm.emit() : -1;
          vm.emit(ifPc, new If(rResult, vm.emitPc()));

          if (skipPc != -1) {
            arguments[2].emit(vm, namespace, rResult);
            vm.emit(skipPc, new Goto(vm.emitPc()));
          }
        });

    bindFunction("interleave",
        new String[]{"input1"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var iterators = new ArrayList<Iterator<Value<?>>>();

          for (final var p : rParameters) {
            final var input = vm.get(p);
            final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input);
            iterators.add(iterator);
          }

          final var result = new ArrayList<Value<?>>();
          var done = false;

          while (!done) {
            done = true;

            for (final var iterator : iterators) {
              if (!iterator.hasNext()) {
                continue;
              }

              result.add(iterator.next());
              done = false;
            }
          }

          vm.set(rResult, new Value<>(iteratorType, result.iterator()));
        });

    bindFunction("iterator",
        new String[]{"sequence"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var s = vm.get(rParameters[0]);
          @SuppressWarnings("unchecked") final var st = (SequenceTrait<Value<?>>) s.type();
          vm.set(rResult, new Value<>(iteratorType, st.iterator(s)));
        });

    bindFunction("length",
        new String[]{"collection"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var c = vm.get(rParameters[0]);
          final var ct = (CollectionTrait) c.type();
          vm.set(rResult, new Value<>(integerType, ct.length(c)));
        });

    bindMacro("let", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var bindingsForm = arguments[0];

          if (!(bindingsForm instanceof VectorForm)) {
            throw new EmitError(bindingsForm.location(), "Invalid bindings: %s.", bindingsForm);
          }

          final var bindings = ((VectorForm) bindingsForm).body();
          final var variables = new TreeMap<Integer, Integer>();

          for (int i = 0; i < bindings.length; i += 2) {
            final var nameForm = bindings[i];

            if (i == bindings.length - 1) {
              throw new EmitError(bindingsForm.location(), "Missing Value.");
            }

            final var valueForm = bindings[i + 1];

            final java.util.function.BiConsumer<IdForm, Integer> bindId = (f, rValue) -> {
              final var name = f.name();
              final var found = namespace.find(name);

              if (found != null && found.type() == variableType) {
                final var rVar = found.as(variableType);
                final var rPreviousValue = vm.allocateRegister();
                variables.put(rPreviousValue, rVar);
                vm.emit(new Get(rVar, rPreviousValue));
                vm.emit(new Get(rValue, rVar));
              } else {
                namespace.bind(name, new Value<>(registerType, rValue));
              }
            };

            class Recursive<T> {
              T function;
            }

            final var recursiveBind = new Recursive<java.util.function.BiConsumer<Form, Integer>>();

            recursiveBind.function = (f, rValue) -> {
              if (f instanceof PairForm pf) {
                final var rLeft = vm.allocateRegister();
                final var rRight = vm.allocateRegister();
                vm.emit(new BreakPair(rValue, rLeft, rRight));
                recursiveBind.function.accept(pf.left(), rLeft);
                recursiveBind.function.accept(pf.right(), rRight);
              } else if (f instanceof IdForm idf) {
                bindId.accept(idf, rValue);
              } else if (!(f instanceof NoneForm)) {
                throw new EmitError(f.location(), "Invalid binding: %s.", f);
              }
            };

            final var rValue = vm.allocateRegister();
            valueForm.emit(vm, namespace, rValue);
            recursiveBind.function.accept(nameForm, rValue);
          }

          for (int i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, namespace, rResult);
          }

          for (final var e : variables.entrySet()) {
            vm.emit(new Get(e.getKey(), e.getValue()));
          }
        });

    bindMacro("load", 1,
        (vm, namespace, location, arguments, rResult) -> {
          vm.evaluate(arguments[0], namespace, rResult);
          final var path = vm.get(rResult).as(pathType);
          vm.set(rResult, null);

          try {
            vm.load(path, namespace);
          } catch (final IOException e) {
            throw new EmitError(location, e.toString());
          }
        });


    bindMacro("map", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var rFunction = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rFunction);
          final var rIterators = new int[arguments.length - 1];
          final var rValues = new int[rIterators.length];

          for (int i = 0; i < rIterators.length; i++) {
            final var r = vm.allocateRegister();
            final var f = arguments[i + 1];
            f.emit(vm, namespace, r);
            vm.emit(new GetIterator(r, r, f.location()));
            rIterators[i] = r;
            rValues[i] = vm.allocateRegister();
          }

          final var rCall = vm.allocateRegister();
          vm.emit(new Set(rResult, new Value<>(Core.vectorType, new ArrayList<>())));
          var iteratePcs = new int[rIterators.length];

          for (int i = 0; i < rIterators.length; i++) {
            iteratePcs[i] = vm.emit();
          }

          vm.emit(new CallIndirect(location, rFunction, rValues, rCall));
          vm.emit(new Push(rResult, rCall, rResult));
          vm.emit(new Goto(iteratePcs[0]));

          for (int i = 0; i < iteratePcs.length; i++) {
            vm.emit(iteratePcs[i], new Iterate(rIterators[i], rValues[i], vm.emitPc()));
          }

          vm.emit(new GetIterator(rResult, rResult, location));
        });

    bindFunction("max",
        new String[]{"value1"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          var lv = vm.get(rParameters[0]);
          final var t = (ComparableTrait) lv.type();

          for (int rParameter : rParameters) {
            final var rv = vm.get(rParameter);

            if (t.compare(lv, rv) == Order.LessThan) {
              lv = rv;
            }
          }

          vm.set(rResult, lv);
        });

    bindFunction("milliseconds",
        new String[]{"n"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var n = vm.get(rParameters[0]).as(integerType);
          vm.set(rResult, new Value<>(timeType, Duration.ofMillis(n)));
        });

    bindFunction("not",
        new String[]{"value"},
        (function, vm, location, namespace, rParameters, rResult) ->
            vm.set(rResult, new Value<>(bitType, !vm.get(rParameters[0]).isTrue())));

    bindMacro("or", 2,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          final var skipPcs = new ArrayList<Integer>();

          for (int i = 1; i < arguments.length; i++) {
            final var orPc = vm.emit();
            skipPcs.add(vm.emit());
            vm.emit(orPc, new If(rResult, vm.emitPc()));
            arguments[i].emit(vm, namespace, rResult);
          }

          for (final var pc : skipPcs) {
            vm.emit(pc, new Goto(vm.emitPc()));
          }
        });

    bindFunction("parse-integer",
        new String[]{"input"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var start = (rParameters.length == 2) ? vm.get(rParameters[1]).as(integerType) : 0;
          final var input = vm.get(rParameters[0]).as(stringType).substring(start);
          final var match = Pattern.compile("^\\s*(\\d+).*").matcher(input);

          if (!match.find()) {
            throw new EvaluationError(location, "Invalid integer: %s", input);
          }

          vm.set(rResult, new Value<>(pairType, new Pair(
              new Value<>(integerType, Integer.valueOf(match.group(1))),
              new Value<>(integerType, match.end(1) + start))));
        });

    bindFunction("path",
        new String[]{"value"},
        (function, vm, location, namespace, rParameters, rResult) ->
            vm.set(rResult, new Value<>(pathType, Paths.get(vm.get(rParameters[0]).as(stringType)))));

    bindMacro("peek", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var rTarget = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rTarget);
          vm.emit(new Peek(rTarget, rResult));
        });

    bindMacro("pop", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var rTarget = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rTarget);
          vm.emit(new Pop(rTarget, rResult));
        });

    bindMacro("push", 2,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          final var rValue = vm.allocateRegister();
          arguments[1].emit(vm, namespace, rValue);
          vm.emit(new Push(rResult, rValue, rResult));
        });

    bindMacro("reduce", 3,
        (vm, namespace, location, arguments, rResult) -> {
          final var rFunction = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rFunction);
          final var rIterator = vm.allocateRegister();
          final var f = arguments[1];
          f.emit(vm, namespace, rIterator);
          vm.emit(new GetIterator(rIterator, rIterator, f.location()));
          final var rValue = vm.allocateRegister();
          arguments[2].emit(vm, namespace, rResult);
          final var iteratePc = vm.emit();
          vm.emit(new CallIndirect(location, rFunction, new int[]{rValue, rResult}, rResult));
          vm.emit(new Goto(iteratePc));
          vm.emit(iteratePc, new Iterate(rIterator, rValue, vm.emitPc()));
        });

    bindFunction("register-count",
        new String[]{},
        (function, vm, location, namespace, rParameters, rResult) ->
            vm.set(rResult, new Value<>(integerType, vm.registerCount())));

    bindMacro("return", 1,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          vm.emit(new Return(rResult));
        });

    bindFunction("say",
        new String[]{"value1"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var what = new StringBuilder();

          for (var i = 0; i < rParameters.length; i++) {
            if (i > 0) {
              what.append(' ');
            }

            what.append(vm.get(rParameters[i]).say());
          }

          System.out.println(what);
          System.out.flush();
        });

    bindMacro("set", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var targetForm = arguments[0];

          if (targetForm instanceof IdForm idf) {
            final var found = namespace.find(idf.name());

            if (found == null) {
              throw new EmitError(location, "Unknown identifier: %s.", idf);
            }

            if (found.type() == registerType) {
              final var r = found.as(registerType);
              arguments[1].emit(vm, namespace, r);

              if (rResult != r) {
                vm.emit(new Get(r, rResult));
              }
            } else {
              throw new EmitError(location, "Invalid target type: %s.", found.type());
            }
          } else {
            throw new EmitError(location, "Invalid target: %s.", targetForm);
          }
        });

    bindFunction("sleep",
        new String[]{"duration"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          try {
            Thread.sleep(vm.get(rParameters[0]).as(timeType));
          } catch (final InterruptedException e) {
            throw new EvaluationError(location, e.toString());
          }
        });

    bindFunction("slice",
        new String[]{"input", "start"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var i = vm.get(rParameters[0]);
          final var it = (IndexedCollectionTrait) i.type();
          final var start = vm.get(rParameters[1]);
          final var end = (rParameters.length == 2) ? null : vm.get(rParameters[2]);
          vm.set(rResult, it.slice(i, start, end));
        });

    bindFunction("reverse-string",
        new String[]{"input"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var result = new StringBuilder(vm.get(rParameters[0]).as(stringType)).reverse().toString();
          vm.set(rResult, new Value<>(stringType, result));
        });

    bindFunction("slurp",
        new String[]{"path"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          try {
            final var p = vm.loadPath().resolve(vm.get(rParameters[0]).as(pathType));
            final String data = Files.readString(p);
            vm.set(rResult, new Value<>(stringType, data));
          } catch (final IOException e) {
            throw new EvaluationError(location, "Failed reading file: %s", e);
          }
        });

    bindFunction("split",
        new String[]{"whole", "separator"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var w = vm.get(rParameters[0]).as(stringType);
          final var s = vm.get(rParameters[1]).as(stringType);
          final String[] parts = w.split(Pattern.quote(s));
          final var result = new ArrayList<Value<?>>();

          for (final var p : parts) {
            result.add(new Value<>(stringType, p));
          }

          vm.set(rResult, new Value<>(iteratorType, result.iterator()));
        });

    bindMacro("tail", 1,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          vm.emit(new Tail(rResult, rResult));
        });

    bindMacro("trace", 0,
        (vm, namespace, location, rParameters, rResult) -> vm.toggleTracing());

    bindFunction("unzip",
        new String[]{"input"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var results = new ArrayList<ArrayList<Value<?>>>();

          final var input = vm.get(rParameters[0]);
          final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input);

          while (iterator.hasNext()) {
            var v = iterator.next();
            var i = 0;

            for (; ; ) {
              if (results.size() <= i) {
                results.add(new ArrayList<>());
              }

              if (v.type() == pairType) {
                final var p = v.as(pairType);
                results.get(i).add(p.left());
                v = p.right();
              } else {
                results.get(i).add(v);
                break;
              }

              i++;
            }
          }

          final var result = results.stream().map((r) -> new Value<>(iteratorType, r.iterator())).iterator();
          vm.set(rResult, new Value<>(iterator2Type, result));
        });

    bindFunction("zip",
        new String[]{"input1"},
        (function, vm, location, namespace, rParameters, rResult) -> {
          final var iterators = new ArrayList<Iterator<Value<?>>>();

          for (final var p : rParameters) {
            final var input = vm.get(p);
            final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input);
            iterators.add(iterator);
          }

          final var result = new ArrayList<Value<?>>();
          var done = false;

          while (!done) {
            Value<?> value = null;

            for (int i = iterators.size() - 1; i >= 0; i--) {
              final var iterator = iterators.get(i);

              if (!iterator.hasNext()) {
                done = true;
                break;
              }

              if (value == null) {
                value = iterator.next();
              } else {
                value = new Value<>(pairType, new Pair(iterator.next(), value));
              }
            }

            if (!done) {
              result.add(value);
            }
          }

          vm.set(rResult, new Value<>(iteratorType, result.iterator()));
        });
  }

  public interface CallableTrait {
    void call(Value<?> target, Vm vm, Namespace namespace, Location location, int[] rParameters, int rResult);
  }

  public interface CollectionTrait {
    int length(final Value<?> value);
  }

  public interface ComparableTrait {
    Order compare(final Value<?> left, final Value<?> right);
  }

  public interface IndexedCollectionTrait {
    Value<?> slice(final Value<?> value, final Value<?> start, final Value<?> end);
  }

  public interface SequenceTrait<T> {
    Iterator<T> iterator(final Value<?> value);
  }

  public static class BitType extends Type<Boolean> {
    public BitType(final String name) {
      super(name);
    }

    public String dump(final Boolean value) {
      return value ? "T" : "F";
    }

    public boolean isTrue(final Boolean value) {
      return value;
    }
  }

  public static class CharacterType extends Type<Character> implements ComparableTrait {
    public CharacterType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final char l = left.as(this);
      final char r = right.as(this);

      if (l < r) {
        return Order.LessThan;
      }

      if (l > r) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public String dump(final Character value) {
      return String.format("\\%c", value);
    }

    public boolean isTrue(final Character value) {
      return value != 0;
    }

    public String say(final Character value) {
      return super.dump(value);
    }
  }

  public static class CollectionType extends Type<Object> {
    public CollectionType(final String name) {
      super(name);
    }
  }

  public static class ComparableType extends Type<Object> {
    public ComparableType(final String name) {
      super(name);
    }
  }

  public static class DotType
      extends Type<Pair>
      implements CallableTrait {

    public DotType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Namespace namespace,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      getReference(target.as(this), vm, namespace, location, rParameters, rResult).
          as(macroReferenceType).
          call(vm, location, rParameters, rResult);
    }

    public String dump(final Pair value) {
      return String.format("%s.%s", value.left().toString(), value.right().toString());
    }

    public MacroReference makeReference(final Value<?> left,
                                        final Value<?> right,
                                        final Vm vm,
                                        final Namespace namespace,
                                        final Location location,
                                        final int[] rParameters,
                                        final int rResult) {
      final var arguments = new Form[rParameters.length + 1];
      arguments[0] = left.newCallTarget(location);

      for (int i = 0; i < rParameters.length; i++) {
        arguments[i + 1] = new RegisterForm(location, rParameters[i]);
      }

      final var startPc = vm.emitPc();
      final var f = new SexprForm(location, right.newCallTarget(location), new SexprForm(location, arguments));
      f.emit(vm, namespace, rResult);
      vm.emit(new Return(rResult));
      return new MacroReference(
          String.format("%s-%s-%d", left, right, rParameters.length),
          startPc, rParameters);
    }

    public Form newCallTarget(final Pair value, final Location location) {
      return new DotForm(location, value.left().newCallTarget(location), value.right().newCallTarget(location));
    }

    public Value<?> getReference(final Pair target,
                                 final Vm vm,
                                 final Namespace namespace,
                                 final Location location,
                                 final int[] rParameters,
                                 final int rResult) {
      final var referenceName = String.format("%s-%s-%d", target.left(), target.right(), rParameters.length);
      var v = namespace.find(referenceName);

      if (v == null) {
        v = new Value<>(Core.macroReferenceType,
            makeReference(target.left(), target.right(), vm, namespace, location, rParameters, rResult));
        namespace.bind(referenceName, v);

        if (rParameters.length > 0) {
          vm.reallocateRegisters();
        }
      }

      return v;
    }
  }

  public static class IndexedCollectionType extends Type<Object> {
    public IndexedCollectionType(final String name) {
      super(name);
    }
  }

  public static class FunctionType extends Type<Function> implements CallableTrait {
    public FunctionType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Namespace namespace,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      target.as(this).call(vm, location, namespace, rParameters, rResult);
    }

    public void emitCall(final Value<?> target,
                         final Vm vm,
                         final Namespace namespace,
                         final Location location,
                         final int[] rParameters,
                         final int rResult) {
      final var function = target.as(this);

      if (function.arity() != -1 && rParameters.length < function.arity()) {
        throw new EmitError(location, "Not enough arguments: %s.", function);
      }

      super.emitCall(target, vm, namespace, location, rParameters, rResult);
    }

    public Form newCallTarget(final Function value, final Location location) {
      return new IdForm(location, value.name());
    }
  }

  public static class IntegerType
      extends Type<Integer>
      implements ComparableTrait, SequenceTrait<Value<Integer>> {
    public IntegerType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final int l = left.as(this);
      final int r = right.as(this);

      if (l < r) {
        return Order.LessThan;
      }

      if (l > r) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public boolean isTrue(final Integer value) {
      return value != 0;
    }

    public Iterator<Value<Integer>> iterator(final Value<?> value) {
      return Stream
          .iterate(0, x -> x + 1)
          .limit(value.as(this))
          .map(v -> new Value<>(Core.integerType, v))
          .iterator();
    }
  }

  public static class IteratorType
      extends Type<Iterator<Value<?>>>
      implements SequenceTrait<Value<?>> {
    public IteratorType(final String name) {
      super(name);
    }

    public Iterator<Value<?>> iterator(final Value<?> value) {
      return value.as(this);
    }
  }

  public static class Iterator2Type
      extends Type<Iterator<Value<Iterator<Value<?>>>>>
      implements SequenceTrait<Value<Iterator<Value<?>>>> {
    public Iterator2Type(final String name) {
      super(name);
    }

    public Iterator<Value<Iterator<Value<?>>>> iterator(final Value<?> value) {
      return value.as(this);
    }
  }

  public static class MacroType extends Type<Macro> implements CallableTrait {
    public MacroType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Namespace namespace,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      target.as(this).call(vm, namespace, location, rParameters, rResult);
    }

    public Form newCallTarget(final Macro value, final Location location) {
      return new IdForm(location, value.name());
    }
  }

  public static class MapType
      extends Type<Map<Value<?>, Value<?>>>
      implements CallableTrait, CollectionTrait, SequenceTrait<Value<?>> {
    public MapType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Namespace namespace,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      final var map = target.as(this);

      switch (rParameters.length) {
        case 1: {
          final var key = vm.get(rParameters[0]);
          final var value = map.get(key);
          vm.set(rResult, (value == null) ? new Value<>(Core.noneType, null) : value);
          break;
        }
        case 2: {
          final var key = vm.get(rParameters[0]);
          final var value = vm.get(rParameters[1]);
          map.put(key, value);
          vm.set(rResult, target);
          break;
        }
        default:
          throw new EvaluationError(location, "Invalid map call.");
      }
    }

    public String dump(final Map<Value<?>, Value<?>> value) {
      final var result = new StringBuilder();
      result.append('{');
      var first = true;

      for (final var e : value.entrySet()) {
        if (!first) {
          result.append(' ');
        }

        if (e.getKey().equals(e.getValue())) {
          result.append(e.getValue().toString());
        } else {
          result.append(e.getKey().toString());
          result.append(':');
          result.append(e.getValue().toString());
        }

        first = false;
      }

      result.append('}');
      return result.toString();
    }

    public boolean equalValues(final Map<Value<?>, Value<?>> left, final Map<Value<?>, Value<?>> right) {
      if (left.size() != right.size()) {
        return false;
      }

      final var li = left.entrySet().iterator();
      final var ri = right.entrySet().iterator();

      while (li.hasNext()) {
        final var le = li.next();
        final var re = ri.next();

        if (!(le.getKey().equals(re.getKey()) && le.getValue().equals(re.getValue()))) {
          return false;
        }
      }

      return true;
    }

    public boolean isTrue(final Map<Value<?>, Value<?>> value) {
      return !value.isEmpty();
    }

    public Iterator<Value<?>> iterator(final Value<?> value) {
      final var items = new ArrayList<Value<?>>();

      for (final var e : value.as(this).entrySet()) {
        if (e.getKey().equals(e.getValue())) {
          items.add(e.getKey());
        } else {
          items.add(new Value<>(Core.pairType, new Pair(e.getKey(), e.getValue())));
        }
      }

      return items.iterator();
    }

    public int length(final Value<?> value) {
      return value.as(this).size();
    }

    public void makeValue(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
      if (rParameters.length % 2 != 0) {
        throw new EvaluationError(location, "Map.make requires an even number of parameters.");
      }

      final var result = new TreeMap<Value<?>, Value<?>>();

      for (int i = 0; i < rParameters.length; i += 2) {
        final var k = vm.get(rParameters[i]);
        final var v = vm.get(rParameters[i + 1]);
        result.put(k, v);
      }

      vm.set(rResult, new Value<>(this, result));
    }
  }

  public static class SetType extends MapType {
    public SetType(final String name) {
      super(name);
    }

    public void makeValue(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
      final var result = new TreeMap<Value<?>, Value<?>>();

      for (int i = 0; i < rParameters.length; i++) {
        final var v = vm.get(rParameters[i]);
        result.put(v, v);
      }

      vm.set(rResult, new Value<>(mapType, result));
    }
  }

  public static class MetaType extends Type<Type<?>> implements CallableTrait {
    public MetaType(final String name) {
      super(name);
    }

    public void call(Value<?> target, Vm vm, Namespace namespace, Location location, int[] rParameters, int rResult) {
      makeMake(target).call(vm, location, namespace, rParameters, rResult);
    }

    public void emitCall(final Value<?> target,
                         final Vm vm,
                         final Namespace namespace,
                         final Location location,
                         final int[] rParameters,
                         final int rResult) {
      vm.emit(new CallDirect(location, new Value<>(functionType, makeMake(target)), rParameters, rResult));
    }

    private Function makeMake(final Value<?> target) {
      return new Function(String.format("%s.make", name()),
          new Function.Parameter[]{},
          (function, _vm, _location, _namespace, _rParameters, _rResult) ->
              target.as(this).makeValue(_vm, _location, _rParameters, _rResult));
    }
  }

  public static class NoneType extends Type<Object> {
    public NoneType(final String name) {
      super(name);
    }

    public String dump(final Object value) {
      return "_";
    }

    public boolean isTrue(final Object value) {
      return false;
    }

    public Value<?> push(final Value<?> target, final Value<?> value) {
      return value;
    }
  }

  public static class PairType
      extends Type<Pair>
      implements ComparableTrait, SequenceTrait<Value<?>> {
    public PairType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final var ll = left.as(this).left();
      final var rl = right.as(this).left();
      final var r1 = ll.compareTo(rl);

      if (r1 < 0) {
        return Order.LessThan;
      }

      if (r1 > 0) {
        return Order.GreaterThan;
      }

      final var lr = left.as(this).right();
      final var rr = right.as(this).right();
      final var r2 = lr.compareTo(rr);

      if (r2 < 0) {
        return Order.LessThan;
      }

      if (r2 > 0) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public String dump(final Pair value) {
      return String.format("%s:%s", value.left().toString(), value.right().toString());
    }

    public boolean equalValues(Pair left, Pair right) {
      for (; ; ) {
        if (!left.left().equals(right.left())) {
          return false;
        }

        if (left.right().type() == pairType) {
          left = left.right().as(this);
          right = right.right().as(this);
        } else {
          return left.right().equals(right.right());
        }
      }
    }

    public boolean isTrue(final Pair value) {
      return value.left().isTrue();
    }

    public Iterator<Value<?>> iterator(Value<?> value) {
      final var result = new ArrayList<Value<?>>();

      while (value.type() == this) {
        final var p = value.as(this);
        result.add(p.left());
        value = p.right();
      }

      result.add(value);
      return result.iterator();
    }

    public Value<?> peek(final Value<?> target) {
      return target.as(this).left();
    }

    public Value<?> pop(final Vm vm, final Value<?> target, final int rTarget) {
      final var p = target.as(this);
      final var result = p.left();
      vm.set(rTarget, p.right());
      return result;
    }

    public Value<?> push(final Value<?> target, final Value<?> value) {
      return new Value<>(this, new Pair(value, target));
    }
  }

  public static class ListType extends PairType {
    public ListType(final String name) {
      super(name);
    }

    public void makeValue(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
      vm.set(rResult, make(vm, vm.get(rParameters[0]), rParameters, 1));
    }

    private Value<?> make(final Vm vm, final Value<?> value, final int[] rParameters, final int i) {
      if (i == rParameters.length) {
        return value;
      }

      return new Value<>(pairType, new Pair(value, make(vm, vm.get(rParameters[i]), rParameters, i + 1)));
    }
  }

  public static class RegisterType extends Type<Integer> implements CallableTrait {
    public RegisterType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Namespace namespace,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      final var t = vm.get(target.as(this));

      if (!(t.type() instanceof Core.CallableTrait)) {
        throw new EvaluationError(location, "Invalid call target: %s.", t);
      }

      ((Core.CallableTrait) t.type()).call(t, vm, namespace, location, rParameters, rResult);
    }

    public void emitId(final Value<?> value, final Vm vm, final Namespace namespace, final int rResult) {
      final var rSource = value.as(this);

      if (rSource != rResult) {
        vm.emit(new Get(rSource, rResult));
      }
    }
  }

  public static class SequenceType extends Type<Object> {
    public SequenceType(final String name) {
      super(name);
    }
  }

  public static class StringType
      extends Type<String>
      implements CallableTrait, CollectionTrait, ComparableTrait, IndexedCollectionTrait,
      SequenceTrait<Value<Character>> {
    public StringType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Namespace namespace,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      final var i = vm.get(rParameters[0]).as(integerType);
      vm.set(rResult, new Value<>(characterType, target.as(this).charAt(i)));
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final var l = left.as(this);
      final var r = right.as(this);
      final var result = l.compareTo(r);

      if (result < 0) {
        return Order.LessThan;
      }

      if (result > 0) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public String dump(final String value) {
      return String.format("\"%s\"", value);
    }

    public boolean isTrue(String value) {
      return !value.isEmpty();
    }

    public Iterator<Value<Character>> iterator(final Value<?> value) {
      return (value.as(this)).codePoints()
          .mapToObj((c) -> new Value<>(Core.characterType, (char) c))
          .iterator();
    }

    public int length(final Value<?> value) {
      return value.as(this).length();
    }


    public void makeValue(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
      final var result = new StringBuilder();

      for (int rParameter : rParameters) {
        result.append(vm.get(rParameter).say());
      }

      vm.set(rResult, new Value<>(stringType, result.toString()));
    }

    public String say(final String value) {
      return value;
    }

    public Value<?> slice(final Value<?> value, final Value<?> start, final Value<?> end) {
      final var si = start.as(Core.integerType);
      final var v = value.as(this);

      final var result = (end == null)
          ? v.substring(si)
          : v.substring(si, end.as(Core.integerType));

      return new Value<>(Core.stringType, result);
    }
  }

  public static class SymbolType extends StringType {
    public SymbolType(final String name) {
      super(name);
    }

    public String dump(final String value) {
      return String.format("'%s", value);
    }

    public boolean isTrue(String value) {
      return true;
    }

    public void makeValue(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
      final var result = new StringBuilder();

      for (int rParameter : rParameters) {
        result.append(vm.get(rParameter).say());
      }

      vm.set(rResult, new Value<>(symbolType, result.toString()));
    }

  }

  public static class TimeType
      extends Type<Duration>
      implements ComparableTrait {
    public TimeType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final var l = left.as(this);
      final var r = right.as(this);
      final var result = l.compareTo(r);

      if (result < 0) {
        return Order.LessThan;
      }

      if (result > 0) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public boolean isTrue(final Duration value) {
      return !value.equals(Duration.ZERO);
    }
  }

  public static class VectorType
      extends Type<ArrayList<Value<?>>>
      implements CallableTrait, CollectionTrait, SequenceTrait<Value<?>> {
    public VectorType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Namespace namespace,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      final var vector = target.as(this);

      switch (rParameters.length) {
        case 1: {
          final var key = vm.get(rParameters[0]);
          final var value = vector.get(key.as(Core.integerType));
          vm.set(rResult, value);
          break;
        }
        case 2: {
          final var key = vm.get(rParameters[0]).as(Core.integerType);
          final var value = vm.get(rParameters[1]);
          vector.set(key, value);
          vm.set(rResult, target);
          break;
        }
        default:
          throw new EvaluationError(location, "Invalid vector call.");
      }
    }

    public String dump(final ArrayList<Value<?>> value) {
      final var result = new StringBuilder();
      result.append('[');
      var first = true;

      for (final var v : value) {
        if (!first) {
          result.append(' ');
        }

        result.append(v.toString());
        first = false;
      }

      result.append(']');
      return result.toString();
    }

    public boolean equalValues(final ArrayList<Value<?>> left, final ArrayList<Value<?>> right) {
      if (left.size() != right.size()) {
        return false;
      }

      final var li = left.iterator();
      final var ri = right.iterator();

      while (li.hasNext()) {
        if (!li.next().equals(ri.next())) {
          return false;
        }
      }
      return true;
    }

    public boolean isTrue(final ArrayList<Value<?>> value) {
      return !value.isEmpty();
    }

    public Iterator<Value<?>> iterator(final Value<?> value) {
      return value.as(this).iterator();
    }

    public int length(final Value<?> value) {
      return value.as(this).size();
    }

    public void makeValue(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
      final var input = vm.get(rParameters[0]);

      @SuppressWarnings("unchecked") final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input);
      final var result = new ArrayList<Value<?>>();

      while (iterator.hasNext()) {
        result.add(iterator.next());
      }

      vm.set(rResult, new Value<>(vectorType, result));
    }

    public Value<?> peek(final Value<?> target) {
      final var t = target.as(this);
      return t.isEmpty() ? new Value<>(Core.noneType, null) : t.getLast();
    }

    public Value<?> pop(final Vm vm, final Value<?> target, final int rTarget) {
      final var t = target.as(this);
      return t.isEmpty() ? new Value<>(Core.noneType, null) : t.removeLast();
    }

    public Value<?> push(final Value<?> target, final Value<?> value) {
      target.as(this).add(value);
      return target;
    }
  }
}
