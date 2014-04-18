/**
 * 
 */
package rinde.sim.pdptw.scenario;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.pdptw.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.scenario.Scenario;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.util.TimeWindow;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A {@link Scenario} that defines a <i>dynamic pickup-and-delivery problem with
 * time windows</i>. It contains all information needed to instantiate an entire
 * simulation.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public abstract class PDPScenario extends Scenario {

  private static final long serialVersionUID = 7258024865764689371L;

  /**
   * New empty instance.
   */
  protected PDPScenario() {
    super();
  }

  protected PDPScenario(Collection<? extends TimedEvent> events,
      Set<Enum<?>> supportedTypes) {
    super(events, supportedTypes);
  }

  public abstract ImmutableList<? extends Model<?>> createModels();

  /**
   * @return The {@link TimeWindow} of the scenario indicates the start and end
   *         of scenario.
   */
  public abstract TimeWindow getTimeWindow();

  /**
   * @return The size of a tick.
   */
  public abstract long getTickSize();

  /**
   * @return The stop condition indicating when a simulation should end.
   */
  public abstract Predicate<SimulationInfo> getStopCondition();

  /**
   * @return The time unit used in the simulator.
   */
  public abstract Unit<Duration> getTimeUnit();

  /**
   * @return The speed unit used in the {@link RoadModel}.
   */
  public abstract Unit<Velocity> getSpeedUnit();

  /**
   * @return The distance unit used in the {@link RoadModel}.
   */
  public abstract Unit<Length> getDistanceUnit();

  public abstract ProblemClass getProblemClass();

  // used to distinguish between two instances from the same class
  public abstract String getProblemInstanceId();

  public interface ProblemClass {

    String getId();
  }

  public static Builder builder(ProblemClass problemClass, String instanceId) {
    return new Builder(problemClass, instanceId);
  }

  static Builder builder(AbstractBuilder<?> base, ProblemClass problemClass,
      String instanceId) {
    return new Builder(Optional.<AbstractBuilder<?>> of(base), problemClass,
        instanceId);
  }

  public static class DefaultScenario extends PDPScenario {
    final ImmutableList<? extends Supplier<? extends Model<?>>> modelSuppliers;
    private final Unit<Velocity> speedUnit;
    private final Unit<Length> distanceUnit;
    private final Unit<Duration> timeUnit;
    private final TimeWindow timeWindow;
    private final long tickSize;
    private final Predicate<SimulationInfo> stopCondition;
    private final ProblemClass problemClass;
    private final String instanceId;

    DefaultScenario(Builder b, List<? extends TimedEvent> events,
        Set<Enum<?>> supportedTypes) {
      super(events, supportedTypes);
      modelSuppliers = b.getModelSuppliers();
      speedUnit = b.speedUnit;
      distanceUnit = b.distanceUnit;
      timeUnit = b.timeUnit;
      timeWindow = b.timeWindow;
      tickSize = b.tickSize;
      stopCondition = b.stopCondition;
      problemClass = b.problemClass;
      instanceId = b.instanceId;
    }

    @Override
    public Unit<Duration> getTimeUnit() {
      return timeUnit;
    }

    @Override
    public TimeWindow getTimeWindow() {
      return timeWindow;
    }

    @Override
    public long getTickSize() {
      return tickSize;
    }

    @Override
    public Unit<Velocity> getSpeedUnit() {
      return speedUnit;
    }

    @Override
    public Unit<Length> getDistanceUnit() {
      return distanceUnit;
    }

    @Override
    public Predicate<SimulationInfo> getStopCondition() {
      return stopCondition;
    }

    @Override
    public ImmutableList<? extends Model<?>> createModels() {
      final ImmutableList.Builder<Model<?>> builder = ImmutableList.builder();
      for (final Supplier<? extends Model<?>> sup : modelSuppliers) {
        builder.add(sup.get());
      }
      return builder.build();
    }

    @Override
    public ProblemClass getProblemClass() {
      return problemClass;
    }

    @Override
    public String getProblemInstanceId() {
      return instanceId;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof DefaultScenario)) {
        return false;
      }
      final DefaultScenario o = (DefaultScenario) other;
      return super.equals(o)
          && Objects.equal(o.modelSuppliers, modelSuppliers)
          && Objects.equal(o.speedUnit, speedUnit)
          && Objects.equal(o.distanceUnit, distanceUnit)
          && Objects.equal(o.timeUnit, timeUnit)
          && Objects.equal(o.timeWindow, timeWindow)
          && Objects.equal(o.tickSize, tickSize)
          && Objects.equal(o.stopCondition, stopCondition)
          && Objects.equal(o.problemClass, problemClass)
          && Objects.equal(o.instanceId, instanceId);
    }
  }

  public static class Builder extends AbstractBuilder<Builder> {
    final ImmutableList.Builder<TimedEvent> eventBuilder;
    final ImmutableSet.Builder<Enum<?>> eventTypeBuilder;
    final ImmutableList.Builder<Supplier<? extends Model<?>>> modelSuppliers;
    final ProblemClass problemClass;
    final String instanceId;

    Builder(ProblemClass pc, String id) {
      this(Optional.<AbstractBuilder<?>> absent(), pc, id);
    }

    Builder(Optional<AbstractBuilder<?>> base, ProblemClass pc, String id) {
      super(base);
      problemClass = pc;
      instanceId = id;
      eventBuilder = ImmutableList.builder();
      eventTypeBuilder = ImmutableSet.builder();
      modelSuppliers = ImmutableList.builder();
    }

    public Builder addEvent(TimedEvent event) {
      eventBuilder.add(event);
      eventTypeBuilder.add(event.getEventType());
      return self();
    }

    public Builder addEvents(Iterable<? extends TimedEvent> events) {
      for (final TimedEvent te : events) {
        addEvent(te);
      }
      return self();
    }

    public Builder addModel(Supplier<? extends Model<?>> model) {
      modelSuppliers.add(model);
      return self();
    }

    public Builder addModels(
        Iterable<? extends Supplier<? extends Model<?>>> models) {
      modelSuppliers.addAll(models);
      return self();
    }

    public DefaultScenario build() {
      return new DefaultScenario(this, eventBuilder.build(),
          eventTypeBuilder.build());
    }

    @Override
    protected Builder self() {
      return this;
    }

    ImmutableList<Supplier<? extends Model<?>>> getModelSuppliers() {
      return modelSuppliers.build();
    }
  }

  static abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
    static final Unit<Length> DEFAULT_DISTANCE_UNIT = SI.KILOMETER;
    static final Unit<Velocity> DEFAULT_SPEED_UNIT = NonSI.KILOMETERS_PER_HOUR;
    static final Unit<Duration> DEFAULT_TIME_UNIT = SI.MILLI(SI.SECOND);
    static final long DEFAULT_TICK_SIZE = 1000L;
    static final TimeWindow DEFAULT_TIME_WINDOW = new TimeWindow(0,
        8 * 60 * 60 * 1000);
    static final Predicate<SimulationInfo> DEFAULT_STOP_CONDITION = StopCondition.TIME_OUT_EVENT;

    Unit<Length> distanceUnit;
    Unit<Velocity> speedUnit;
    Unit<Duration> timeUnit;
    long tickSize;
    TimeWindow timeWindow;
    Predicate<SimulationInfo> stopCondition;

    AbstractBuilder() {
      this(Optional.<AbstractBuilder<?>> absent());
    }

    AbstractBuilder(AbstractBuilder<?> copy) {
      this(Optional.<AbstractBuilder<?>> of(copy));
    }

    AbstractBuilder(Optional<AbstractBuilder<?>> copy) {
      if (copy.isPresent()) {
        distanceUnit = copy.get().distanceUnit;
        speedUnit = copy.get().speedUnit;
        timeUnit = copy.get().timeUnit;
        tickSize = copy.get().tickSize;
        timeWindow = copy.get().timeWindow;
        stopCondition = copy.get().stopCondition;
      }
      else {
        distanceUnit = DEFAULT_DISTANCE_UNIT;
        speedUnit = DEFAULT_SPEED_UNIT;
        timeUnit = DEFAULT_TIME_UNIT;
        tickSize = DEFAULT_TICK_SIZE;
        timeWindow = DEFAULT_TIME_WINDOW;
        stopCondition = DEFAULT_STOP_CONDITION;
      }
    }

    protected abstract T self();

    public T timeUnit(Unit<Duration> tu) {
      timeUnit = tu;
      return self();
    }

    public T tickSize(long ts) {
      tickSize = ts;
      return self();
    }

    public T speedUnit(Unit<Velocity> su) {
      speedUnit = su;
      return self();
    }

    public T distanceUnit(Unit<Length> du) {
      distanceUnit = du;
      return self();
    }

    public T scenarioLength(long length) {
      timeWindow = new TimeWindow(0, length);
      return self();
    }

    public T stopCondition(Predicate<SimulationInfo> condition) {
      stopCondition = condition;
      return self();
    }
  }
}