package io.roach.spring.statemachine;

import java.util.EnumSet;
import java.util.Optional;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.guard.PaymentIdGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import io.roach.spring.statemachine.action.AbortAction;
import io.roach.spring.statemachine.action.AuthorizeAction;
import io.roach.spring.statemachine.action.CancelAction;
import io.roach.spring.statemachine.action.CaptureAction;
import io.roach.spring.statemachine.action.ErrorAction;
import io.roach.spring.statemachine.action.ReverseAction;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfiguration extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ErrorAction errorAction;

    @Autowired
    private AuthorizeAction authAction;

    @Autowired
    private AbortAction abortAction;

    @Autowired
    private CaptureAction captureAction;

    @Autowired
    private CancelAction cancelAction;

    @Autowired
    private ReverseAction reverseAction;

    @Autowired
    private PaymentIdGuard paymentIdGuard;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.CREATED)
                .states(EnumSet.allOf(PaymentState.class))
//                .end(PaymentState.CAPTURED)
                .end(PaymentState.REVERSED)
                .end(PaymentState.REVERSE_ERROR)
                .end(PaymentState.AUTH_ERROR)
                .end(PaymentState.CAPTURE_ERROR)
                .end(PaymentState.ABORTED)
                .end(PaymentState.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                // Branches from state CREATED
                .withExternal().source(PaymentState.CREATED).target(PaymentState.CREATED)
                .event(PaymentEvent.AUTHORIZE)
                .action(Actions.errorCallingAction(authAction, errorAction)).guard(paymentIdGuard)
                .and()
                .withExternal().source(PaymentState.CREATED).target(PaymentState.AUTHORIZED)
                .event(PaymentEvent.AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.CREATED).target(PaymentState.AUTH_ERROR) // end state
                .event(PaymentEvent.AUTH_DECLINED)
                .and()
                .withExternal().source(PaymentState.CREATED).target(PaymentState.ABORTED) // end state
                .event(PaymentEvent.ABORT)
                .action(Actions.errorCallingAction(abortAction, errorAction))

                // Branches from state AUTHORIZED
                .and()
                .withExternal().source(PaymentState.AUTHORIZED).target(PaymentState.AUTHORIZED)
                .event(PaymentEvent.CAPTURE)
                .action(Actions.errorCallingAction(captureAction, errorAction))
                .and()
                .withExternal().source(PaymentState.AUTHORIZED).target(PaymentState.CAPTURED)
                .event(PaymentEvent.CAPTURE_SUCCESS)
                .and()
                .withExternal().source(PaymentState.AUTHORIZED).target(PaymentState.CAPTURE_ERROR) // end state
                .event(PaymentEvent.CAPTURE_FAILED)
                .and()
                .withExternal().source(PaymentState.AUTHORIZED).target(PaymentState.CANCELLED) // end state
                .event(PaymentEvent.CANCEL)
                .action(Actions.errorCallingAction(cancelAction, errorAction))

                // Branches from state CAPTURED
                .and()
                .withExternal().source(PaymentState.CAPTURED).target(PaymentState.CAPTURED)
                .event(PaymentEvent.REVERSE)
                .action(Actions.errorCallingAction(reverseAction, errorAction))
                .and()
                .withExternal().source(PaymentState.CAPTURED).target(PaymentState.REVERSED) // end state
                .event(PaymentEvent.REVERSE_SUCCESS)
                .and()
                .withExternal().source(PaymentState.CAPTURED).target(PaymentState.REVERSE_ERROR)
                .event(PaymentEvent.REVERSE_FAILED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
//                logger.debug("State changed from: {} to: {}", ofNullable(from), ofNullable(to));
            }

            @Override
            public void transition(Transition<PaymentState, PaymentEvent> transition) {
                logger.warn("Transitioning from {} -> {}",
                        ofNullable(transition.getSource()),
                        ofNullable(transition.getTarget()));
            }

            @Override
            public void eventNotAccepted(Message<PaymentEvent> event) {
                logger.error("Event rejected: {}", event);
            }

            private Object ofNullable(State s) {
                return Optional.ofNullable(s)
                        .map(State::getId)
                        .orElse(null);
            }
        };

        config.withConfiguration()
                .listener(adapter);
    }
}