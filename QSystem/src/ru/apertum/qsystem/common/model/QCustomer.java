/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.common.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ru.apertum.qsystem.server.controller.Executer;
import ru.apertum.qsystem.common.CustomerState;
import ru.apertum.qsystem.common.QConfig;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.extra.IChangeCustomerStateEvent;
import ru.apertum.qsystem.server.Spring;
import ru.apertum.qsystem.server.model.IidGetter;
import ru.apertum.qsystem.server.model.QOffice;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.response.QRespEvent;
import ru.apertum.qsystem.server.model.results.QResult;

/**
 * @author Evgeniy Egorov ?????????? ?????? ????????????? "?????????". ???????????? ??? ??????????? ??????? ???????. ???? ???????????? ????, ?? ??????????
 * ?????????? ??? ????? ??????????. ?????! ?????? ????????? ?????? ????????? ??? ??? ?????????, ???????? ??? ??? ????????.
 */
@Entity
@Table(name = "clients")
public final class QCustomer implements Comparable<QCustomer>, Serializable, IidGetter {

    @Transient
    private final LinkedList<QRespEvent> resps = new LinkedList<>();
    /**
     * ?????? ????? ? ??????? ?????????? ????????? ????? ????????? ????? ?????? ??? ???????? ?????????? ? ?????? ??????. ??? ???????? ????? ?????? ?? ?????? ?
     * ??????? ??.
     */
    private final LinkedList<QService> serviceBack = new LinkedList<>();
    @Expose
    @SerializedName("complex_id")
    public LinkedList<LinkedList<LinkedList<Long>>> complexId = new LinkedList<>();
    @Expose
    @SerializedName("id")
    private Long id = new Date().getTime();
    /**
     * ???????? "??????????" ???????????? ?????, ?????? ?? ???? ??????? ????? ???? ? ?????????? ???????????? ????? - ????? ?????
     */
    @Expose
    @SerializedName("number")
    private Integer number;

    @Expose
    @SerializedName("stateIn")
    private Integer stateIn;
    /**
     * ???????? "??????????" ????????? ?????????, ?????? ?? ???? ??????? ????? ??? ?????? ?????????? ? ?????????? ??? ????????? ?????? ?????? ???? ???????? ???
     * ????? ? ????? ? ??? ?????? ????????? ? ???? ?????????. ???? ?????? ??????? ? ??, ?? ?????? ?? ????????? ????????????? ????????? ??? ???. ??? ??? ????
     * ?????-?? ???????? ????????? ? ??? ????????? ?????? ????????? ? ??, ?? ??? ? ???? ?????????? ??? ???????? ???????? ??????????????, ? ?? ????? ?????? ,
     * ???? ????, ??? ???????? ? ?????? ?????????, ???????? ?? ??????????????.
     *
     * ????????? ???????
     *
     * @see ru.apertum.qsystem.common.Uses
     */
    @Expose
    @SerializedName("state")
    private CustomerState state;
    /**
     * ????????? "??????????"
     */
    @Expose
    @SerializedName("priority")
    private Integer priority;
    /**
     * ? ????? ?????? ?????. ????? ??? ??????????.
     */
    @Expose
    @SerializedName("to_service")
    private QService service;
    /**
     * ????????? ?????? ? ?????????????
     */
    private QResult result;
    /**
     * ??? ??? ????????????. ????? ??? ??????????. :: Who processes it. It is necessary for statistics.
     */
    @Expose
    @SerializedName("from_user")
    private QUser user;
    /**
     * ??????? ??????, ? ??????? ????? ????????.
     *
     * ?????? ????????.
     */
    @Expose
    @SerializedName("prefix")
    private String prefix;
    @Expose
    @SerializedName("welcome_time")
    private Date welcomeTime;
    @Expose
    @SerializedName("invite_time")
    private Date inviteTime;
    @Expose
    @SerializedName("stand_time")
    private Date standTime;
    @Expose
    @SerializedName("start_time")
    private Date startTime;
    private Date callTime;
    @Expose
    @SerializedName("finish_time")
    private Date finishTime;
    @Expose
    @SerializedName("input_data")
    private String input_data = "";
    @Expose
    @SerializedName("need_back")
    private boolean needBack = false;
    @Expose
    @SerializedName("in_sequence")
    private boolean in_sequence = false;
    @Expose
    @SerializedName("log_waitqueue")
    public boolean log_waitqueue;
    @Expose
    @SerializedName("spId")
    public Long spId = 0L;
    @Expose
    @SerializedName("stateInPrevious")
    private Integer stateInPrevious = 0;

    /**
     * ???????????? ?????? ? ????????? ??? ????????? ? ???????? ? ?????????? :: Comments and users about the custodian when redirecting and sending to deferred
     */
    @Expose
    @SerializedName("temp_comments")
    private String tempComments = "";

    //  Quick transaction data.
    @Expose
    @SerializedName("temp_qtxn")
    private boolean tempQuickTxn = false;

    /**
     * name of the user who added customer in the list
     */
    @Expose
    @SerializedName("added_by")
    private String addedBy = "";
    /**
     *
     */
    @Expose
    @SerializedName("post_atatus")
    private String postponedStatus = "";
    /**
     * ?????? ???????????? ? ???????. 0 - ?????????;
     */
    @Expose
    @SerializedName("postpone_period")
    private int postponPeriod = 0;
    /**
     * ID ???? ??? ????? ???????????, NULL ??? ????
     */
    @Expose
    @SerializedName("is_mine")
    private Long isMine = null;
    /**
     * ?????????? ????????? ??????? ????? ???????
     */
    @Expose
    @SerializedName("recall_cnt")
    private Integer recallCount = 0;
    private long startPontpone = 0;
    private long finishPontpone = 0;
    //    ANDREW added quantity for insert into DB
    @Expose
    @SerializedName("service_quantity")
    private String quantity = "1";
    @Expose
    @SerializedName("channels")
    private String channels;
    @Expose
    @SerializedName("channelsIndex")
    private int channelsIndex;
    private LinkedList<QService> PreviousList = new LinkedList<>();
    @Expose
    @SerializedName("office")
    private QOffice office;

    public QCustomer() {
        id = new Date().getTime();
    }

    /**
     * ??????? ??????? ???? ?????? ??? ????? ? ???????. ??????? ?? ?????????, ?.?. ??? ?? ????? ?? ?????? ???? ??? ?????????. ???????? ????????? ?????? -
     * ?????????? ? ?? ????????.
     *
     * @param number ????? ??????? ? ???????
     */
    public QCustomer(int number) {
        this.number = number;
        id = new Date().getTime();
        setStandTime(new Date()); // ???????? ?? ????????????? ??? ??????????
        // ??? ????????? ???????? ????????? ?? ?????? ???? ????? ??????????? ? ????? ?????? ??? ????????? ????????? ? ???
        //QLog.l().logger().debug("??????? ????????? ? ??????? " + number);
    }

    @Id
    @Column(name = "id")
    @Override
    //@GeneratedValue(strategy = GenerationType.AUTO) ??????????? ?????????? ????? ??????? ????????.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "number")
    public int getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Column(name = "state_in")
    public Integer getStateIn() {
        return stateIn;
    }

    public void setStateIn(Integer stateIn) {
        this.stateIn = stateIn;
    }

    public String currentStateIn() {
        switch (state) {
            case STATE_DEAD:
                return "Deleted By default";
            case STATE_WAIT:
                return "Waiting in Line";
            case STATE_WAIT_AFTER_POSTPONED:
                return "Waiting after postponed";
            case STATE_WAIT_COMPLEX_SERVICE:
                return "Waiting after postponed";
            case STATE_INVITED:
                return "Invited";
            case STATE_INVITED_SECONDARY:
                return "Re-Invited";
            case STATE_REDIRECT:
                return "Redirected";
            case STATE_WORK:
                return "Began to work";
            case STATE_WORK_SECONDARY:
                return "Began work again";
            case STATE_BACK:
                return "Comes back after redirect";
            case STATE_FINISH:
                return "Finished";
            case STATE_POSTPONED:
                return "Postponed";
            case STATE_POSTPONED_REDIRECT:
                return "Postponed";
            default:
                return "Undefined";
        }
    }

    public void setState(CustomerState state) {
        setState(state, new Long(-1));
    }

    /**
     * ?????????? ??? ????????? ? ???????? ????? ?????????
     *
     * @param newServiceId - ??? ????????? ? ???????? ????? ????????? ??? ????? ID ??? ?????? ???? ?????????? ??? ??????????, ?????? ?????? ? ????????? ??? ???
     * ???????, ?.?. ??? ? ??????? ????????? ? ??? ????????
     */
    public void setState(CustomerState state, Long newServiceId) {
        this.state = state;
        if (stateIn == null) {
            stateInPrevious = 0;
            QLog.l().logger().debug("==> ERROR: Current stateIn is null");
        }
        else {
            QLog.l().logger().debug("==> All OK: Current stateIn is " + stateIn);
            stateInPrevious = stateIn;
        }
        stateIn = state.ordinal();
        QLog.l().logger().debug("    --> New stateIn is " + stateIn);

        // ????? ????? ??????? ?? ????? ????????? ? ????? ? ?? ??? ???????????
        if (getUser() != null && getUser().getShadow() != null) {
            getUser().getShadow().setCustomerState(state);
        }

        switch (state) {
            case STATE_DEAD:
                //QLog.l().logger().debug("??????: ???????? ? ??????? \"" + getPrefix() + getNumber()
                //        + "\" ???? ????? ?? ??????");
                getUser().getPlanService(getService()).inkKilled();
                // ??? ? ???, ???????? ???? ????? ???????? ????????????. ???????? ????????? ? ????
                // ?????? ?????_???? ???? ??????????, ??? ????, ? ?????_???? ????, ??????????
                setStartTime(new Date());
                setFinishTime(new Date());
                break;
            case STATE_WAIT:
                //QLog.l().logger().debug(
                //        "??????: ???????? ?????? ? ???? ? ??????? \"" + getPrefix() + getNumber()
                //                + "\"");
                break;
            case STATE_WAIT_AFTER_POSTPONED:
                //QLog.l().logger().debug(
                //        "??????: ???????? ??? ????????? ?? ?????????? ?? ????????? ??????? ? ???? ? ??????? \""
                //                + getPrefix() + getNumber() + "\"");
                break;
            case STATE_WAIT_COMPLEX_SERVICE:
                //QLog.l().logger().debug(
                //        "??????: ???????? ??? ????? ????????? ? ??????? ?.?. ?????? ??????????? ? ???? ? ??????? \""
                //                + getPrefix() + getNumber() + "\"");
                break;
            case STATE_INVITED:
                //QLog.l().logger()
                //        .debug("??????: ?????????? ????????? ? ??????? \"" + getPrefix()
                //                + getNumber()
                //                + "\"");
                break;
            case STATE_INVITED_SECONDARY:
                //QLog.l().logger().debug(
                //        "??????: ?????????? ???????? ? ??????? ????????? ????????? ? ??????? \""
                //                + getPrefix()
                //                + getNumber() + "\"");
                break;
            case STATE_REDIRECT:
                //                QLog.l().logger()
                //                        .debug("??????: ????????? ??????????? ? ??????? \"" + getPrefix()
                //                                + getNumber()
                //                                + "\"");
                getUser().getPlanService(getService())
                        .inkWorked(new Date().getTime() - getStartTime().getTime());
                // ???????? ????????? ? ????
                this.refreshQuantity();
                break;
            case STATE_WORK:
                //QLog.l().logger()
                //        .debug("?????? ???????? ? ?????????? ? ??????? \"" + getPrefix()
                //                + getNumber()
                //                + "\"");
                getUser().getPlanService(getService())
                        .upWait(new Date().getTime() - getStandTime().getTime());
                break;
            case STATE_WORK_SECONDARY:
                //                QLog.l().logger().debug(
                //                        "??????: ????? ?? ??????? ?????? ???????? ? ?????????? ? ??????? \""
                //                                + getPrefix()
                //                                + getNumber() + "\"");
                break;
            case STATE_BACK:
                //                QLog.l().logger().debug("??????: ???????? ? ??????? \"" + getPrefix() + getNumber()
                //                        + "\" ?????? ? ??????? ??????");
                break;
            case STATE_FINISH:
                //QLog.l().logger()
                //        .debug("??????: ? ?????????? ? ??????? \"" + getPrefix() + getNumber()
                //                + "\" ????????? ????????");
                getUser().getPlanService(getService())
                        .inkWorked(new Date().getTime() - getStartTime().getTime());
                // ???????? ????????? ? ???? :: Keep the customizer in the database
                break;
            case STATE_POSTPONED:
                //                QLog.l().logger().debug("???????? ? ??????? \"" + getPrefix() + getNumber()
                //                        + "\" ???? ????? ? ?????? ??????????");
                getUser().getPlanService(getService())
                        .inkWorked(new Date().getTime() - getStartTime().getTime());
                // ???????? ????????? ? ???? :: Keep the customizer in the database
                break;
            case STATE_POSTPONED_REDIRECT:
                //                QLog.l().logger().debug("Customer to postpone prefix \"" + getPrefix() + getNumber()
                //                        + "\" ???? ????? ? ?????? ??????????");
                startTime = standTime;
                getUser().getPlanService(getService())
                        .inkWorked(new Date().getTime() - getStartTime().getTime());
                // ???????? ????????? ? ???? :: Keep the customizer in the database
                break;
            case STATE_INACCURATE_TIME:
                //                QLog.l().logger()
                //                        .debug("??????: ? ?????????? ? ??????? \"" + getPrefix() + getNumber()
                //                                + "\" ????????? ????????");
                getUser().getPlanService(getService())
                        .inkWorked(new Date().getTime() - getStartTime().getTime());
                break;
        }

        // For now, no Snowplow calls, log to see if this is where they should go.
        //        QLog.l().logQUser().debug("==> Changing customer state:");
        //        if (this.getUser() == null) {
        //            QLog.l().logQUser().debug("    --> CSR:    is null");
        //        }
        //        else {
        //            QLog.l().logQUser().debug("    --> CSR:    " + this.getUser().getName());
        //        }
        //        if (this.getOffice() == null) {
        //            QLog.l().logQUser().debug("    --> Office: is null");
        //        }
        //        else {
        //            QLog.l().logQUser().debug("    --> Office: " + this.getOffice().getName());
        //        }
        //        QLog.l().logQUser().debug("    --> Cust:   " + this.getId());
        //        if (this.getService() == null) {
        //            QLog.l().logQUser().debug("    --> Svc:    is null");
        //        }
        //        else {
        //            QLog.l().logQUser().debug("    --> Svc:    " + this.getService().getName());
        //        }
        //        QLog.l().logQUser().debug("    --> State:  " + this.getStateIn());
        //        QLog.l().logQUser().debug("    --> SPId:   " + this.getSpId().toString());

        //  Make Snowplow call.
        Executer.getInstance().SnowplowLogEvent(this);

        saveToSelfDB();

        // ????????? ????????????? ????????? :: Support extensibility plug-ins
        for (final IChangeCustomerStateEvent event : ServiceLoader
                .load(IChangeCustomerStateEvent.class)) {
            QLog.l().logger().info(
                    "????? SPI ??????????. ????????: :: Call the SPI extension. Description: "
                            + event
                                    .getDescription());
            try {
                event.change(this, state, newServiceId);
            }
            catch (Throwable tr) {
                QLog.l().logger().error(
                        "????? SPI ?????????? ?????????? ???????. ????????: :: The SPI extension call failed. Description:"
                                + tr);
            }
        }
    }

    public void addNewRespEvent(QRespEvent event) {
        resps.add(event);
    }

    public void save() {
        saveToSelfDB();
    }

    private void saveToSelfDB() {
        // ???????? ????????? ? ????
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SomeTxName");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = Spring.getInstance().getTxManager().getTransaction(def);
        try {
            if (input_data == null) { // ??? ??? ???? ?????? ????????? ?????????? ?? ?? ??? ????????? ?????? ?? ???, ? ??? ???? ???? ????? ?????
                //                /Here is the same zhed by the pull of the pull to set the contention that the entered data is not zero, and they rarely need this input
                input_data = "";
            }

            List<Integer> finishStates = Arrays.asList(0, 10, 13);
            if (finishStates.contains(getStateIn())) {
                // QLog.l().logQUser().info("Client is in finish state, clear comments...");
                setTempComments("");
            }

            Spring.getInstance().getHt().saveOrUpdate(this);
            //QLog.l().logQUser().info("Saved customer");
            // ???????. ???? ???????? ??????? ?????? ?????? ??? ????? ? ??, ?.?. ?? ????? ?????? ??? ? ???.
            // Crutch. If the customizer left a comment before getting into the database, ie. While working with him.
            if (resps.size() > 0) {
                Spring.getInstance().getHt().saveAll(resps);
                resps.clear();
            }
        }
        catch (Exception ex) {
            Spring.getInstance().getTxManager().rollback(status);
            throw new ServerException(
                    "?????? ??? ?????????? :: Error while saving \n" + ex.toString() + "\n" + Arrays
                            .toString(ex.getStackTrace()));
        }
        Spring.getInstance().getTxManager().commit(status);
        //QLog.l().logger().debug("?????????.");
    }

    @Transient
    public CustomerState getState() {
        return state;
    }

    public void setState(Integer state) {
        CustomerState customerState = CustomerState.STATE_DEAD;

        switch (state) {
            case 0:
                customerState = CustomerState.STATE_DEAD;
                break;
            case 1:
                customerState = CustomerState.STATE_WAIT;
                break;
            case 2:
                customerState = CustomerState.STATE_WAIT_AFTER_POSTPONED;
                break;
            case 3:
                customerState = CustomerState.STATE_WAIT_COMPLEX_SERVICE;
                break;
            case 4:
                customerState = CustomerState.STATE_INVITED;
                break;
            case 5:
                customerState = CustomerState.STATE_INVITED_SECONDARY;
                break;
            case 6:
                customerState = CustomerState.STATE_REDIRECT;
                break;
            case 7:
                customerState = CustomerState.STATE_WORK;
                break;
            case 8:
                customerState = CustomerState.STATE_WORK_SECONDARY;
                break;
            case 9:
                customerState = CustomerState.STATE_BACK;
                break;
            case 10:
                customerState = CustomerState.STATE_FINISH;
                break;
            case 11:
                customerState = CustomerState.STATE_POSTPONED;
                break;
            case 12:
                customerState = CustomerState.STATE_POSTPONED_REDIRECT;
                break;
            case 13:
                customerState = CustomerState.STATE_INACCURATE_TIME;
                break;
            default:
                customerState = CustomerState.STATE_DEAD;
                break;
        }
        QLog.l().logQUser().debug(customerState);
        setState(customerState);
    }

    public void setStateWithoutSave(Integer state) {
        CustomerState customerState = CustomerState.STATE_DEAD;

        switch (state) {
            case 0:
                customerState = CustomerState.STATE_DEAD;
                break;
            case 1:
                customerState = CustomerState.STATE_WAIT;
                break;
            case 2:
                customerState = CustomerState.STATE_WAIT_AFTER_POSTPONED;
                break;
            case 3:
                customerState = CustomerState.STATE_WAIT_COMPLEX_SERVICE;
                break;
            case 4:
                customerState = CustomerState.STATE_INVITED;
                break;
            case 5:
                customerState = CustomerState.STATE_INVITED_SECONDARY;
                break;
            case 6:
                customerState = CustomerState.STATE_REDIRECT;
                break;
            case 7:
                customerState = CustomerState.STATE_WORK;
                break;
            case 8:
                customerState = CustomerState.STATE_WORK_SECONDARY;
                break;
            case 9:
                customerState = CustomerState.STATE_BACK;
                break;
            case 10:
                customerState = CustomerState.STATE_FINISH;
                break;
            case 11:
                customerState = CustomerState.STATE_POSTPONED;
                break;
            case 12:
                customerState = CustomerState.STATE_POSTPONED_REDIRECT;
                break;
            case 13:
                customerState = CustomerState.STATE_INACCURATE_TIME;
                break;
            default:
                customerState = CustomerState.STATE_DEAD;
                break;
        }

        this.state = customerState;
    }

    @Transient
    public IPriority getPriority() {
        if (this.priority == null) {
            this.priority = 0;
            //QLog.l().logger().debug("==> getPriority(): this.priority is " + priority);
        }
        return new Priority(priority);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String taskPriority() {
        switch (priority) {
            case Uses.PRIORITY_LOW:
                return "Low";
            case Uses.PRIORITY_NORMAL:
                return "Normal";
            case Uses.PRIORITY_HI:
                return "High";
            case Uses.PRIORITY_VIP:
                return "VIP";
            default:
                return "Undefined";
        }
    }

    /**
     * ????????? ??????????? ??? ?????? ???????. ????????? ????????? ??????????. ??????? ?? ??????????, ????? ?? ???????
     *
     * @return ???????????? ????????? "?????????? ???????"(????????? ???? ????? ?? ?????? "? ????????? ??????? ??? ??? ? ??????????") 1 - "?????????? ???????"
     * ??? ???????? ? ?????????, -1 - "?????????? ??????" ??? ???????? ? ?????????, 0 - ???????????? -1 - ??????? ?????????? ??? ???????? ?? ??????????, ?.?.
     * ????? ?????? 1 - ?????????? ????? ??? ???????? ?? ??????????, ?.?. ????? ???????
     */
    @Override
    public int compareTo(QCustomer customer) {
        int resultCmp = -1 * getPriority()
                .compareTo(customer.getPriority()); // (-1) - ?.?.  ??????? ????????? ??????? ??????????

        if (resultCmp == 0) {
            if (this.getStandTime().before(customer.getStandTime())) {
                resultCmp = -1;
            }
            else if (this.getStandTime().after(customer.getStandTime())) {
                resultCmp = 1;
            }
        }

        if (resultCmp == 0) {
            //QLog.l().logger().warn("Customers cannot be equal!!  This should never happen.");
            //QLog.l().logger().debug("--> Customer priority/time equal.  Identical customer?");
            resultCmp = -1;
        }

        return resultCmp;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    public QService getService() {
        return service;
    }

    /**
     * ????????? ????????? ???????? ?????? ??????? ???, ????????, ???????. ?????? ??????? ???????? ??? ? ????????. ??? ?????????? ????????? ? ??????
     * addCustomer() ?????????? ???? ????? + ???????????? ???????, ???? ????? ??????? ?? ???????? ? XML-???? ?????????
     *
     * @param service ?? ?????????? ??? NULL
     */
    public void setService(QService service) {
        this.service = service;
        // ??????? ??? ????????? ??????????? ??? ??? ????????, ???? ??? ? ?? ??????.
        if (getPrefix() == null) {
            setPrefix(service.getPrefix());
        }
        //QLog.l().logger().debug(
        //        "??????? \"" + getFullNumber() + "\" ????????? ? ?????? \"" + service.getName()
        //                + "\"");
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    public QResult getResult() {
        return result;
    }

    public void setResult(QResult result) {
        this.result = result;
        if (result == null) {
            //QLog.l().logger().debug("?????????? ????????? ?????? ? ?????????? ?? ?????????");
        }
        else {
            //QLog.l().logger()
            //        .debug("?????????? ????????? ?????? ? ??????????: \"" + result.getName()
            //                + "\"");
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public QUser getUser() {
        return user;
    }

    public void setUser(QUser user) {
        this.user = user;
        String msg = "??????? \"" + getPrefix() + getNumber() + (user == null
                ? " ????? ????, ??? ?? ??? ?? ???????\"" : " ?????????? ????? \"" + user.getName()
                        + "\"");
        //        QLog.l().logger().debug("??????? \"" + getPrefix() + getNumber() + (user == null
        //                ? " ????? ????, ??? ?? ??? ?? ???????\""
        //                : " ?????????? ????? \"" + user.getName() + "\""));
    }

    @Column(name = "service_prefix")
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    @Transient()
    public String getFullNumber() {
        return "" + getPrefix() + QConfig.cfg().getNumDivider(getPrefix()) + getNumber();
    }

    @Column(name = "welcome_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getWelcomeTime() {
        return welcomeTime;
    }

    public void setWelcomeTime(Date date) {
        this.welcomeTime = date;
    }

    @Column(name = "invite_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getInviteTime() {
        return inviteTime;
    }

    public void setInviteTime(Date date) {
        this.inviteTime = date;
    }

    @Column(name = "stand_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStandTime() {
        return standTime;
    }

    public void setStandTime(Date date) {
        this.standTime = date;
    }

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date date) {
        this.startTime = date;
    }

    public String standTimeinHHMMSS() {
        TimeZone currentTimeZone = Calendar.getInstance().getTimeZone();
        DateFormat zoneTimeFormat = Uses.FORMAT_HH_MM_SS;

        if (Uses.userTimeZone == null) {
            zoneTimeFormat.setTimeZone(currentTimeZone);
        }
        else {
            zoneTimeFormat.setTimeZone(Uses.userTimeZone);
        }

        return zoneTimeFormat.format(standTime);
    }

    @Transient
    public Date getCallTime() {
        return callTime;
    }

    public void setCallTime(Date date) {
        this.callTime = date;
    }

    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date date) {
        this.finishTime = date;
    }

    /**
     * ????????? ?????????? ?????? ?? ?????? ???????????.
     */
    @Column(name = "input_data")
    public String getInput_data() {
        return input_data;
    }

    public void setInput_data(String input_data) {
        this.input_data = input_data;
    }

    /**
     * ??? ????????? ???? ???? ???????. ?? ??????? ?????? ??? ????????
     *
     * @param service ? ??? ?????? ????? ???????
     */
    public void addServiceForBack(QService service) {
        serviceBack.addFirst(service);
        needBack = !serviceBack.isEmpty();
    }

    /**
     * ???? ??????? ???? ?????? ????????? ?? ???????? ?????????????
     *
     * @return ??????? ? ??? ??????
     */
    @Transient
    public QService getServiceForBack() {
        needBack = serviceBack.size() > 1;
        return serviceBack.pollFirst();
    }

    public boolean needBack() {
        return needBack;
    }

    @Column(name = "comments")
    public String getTempComments() {
        return tempComments;
    }

    public void setTempComments(String tempComments) {
        //        QLog.l().logger().debug(">>> Cust comments: Old=" + this.tempComments + "; New="
        //                + tempComments);
        this.tempComments = tempComments;
        //        QLog.l().logQUser().debug("\n\nIN CUSTOMER Postponed!!:\n" + this.tempComments + "\n\n\n");
    }

    @Column(name = "quick_txn")
    public boolean getTempQuickTxn() {
        return tempQuickTxn;
    }

    public void setTempQuickTxn(boolean tempQuickTxn) {
        this.tempQuickTxn = tempQuickTxn;
    }

    @Transient
    public String getStringQuickTxn() {
        return (getTempQuickTxn() ? "Yes" : "No");
    }

    @Transient
    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    @Transient
    public String getPostponedStatus() {
        return postponedStatus;
    }

    public void setPostponedStatus(String postponedStatus) {
        this.postponedStatus = postponedStatus;
    }

    @Transient
    public int getPostponPeriod() {
        return postponPeriod;
    }

    public void setPostponPeriod(int postponPeriod) {
        this.postponPeriod = postponPeriod;
        startPontpone = new Date().getTime();
        finishPontpone = startPontpone + postponPeriod * 1000 * 60;
    }

    @Transient
    public Long getIsMine() {
        return isMine;
    }

    public void setIsMine(Long userId) {
        this.isMine = userId;
    }

    @Transient
    public Integer getRecallCount() {
        return recallCount;
    }

    public void setRecallCount(Integer recallCount) {
        this.recallCount = recallCount;
    }

    public void upRecallCount() {
        this.recallCount++;
    }

    @Transient
    public long getFinishPontpone() {
        return finishPontpone;
    }

    /**
     * ?????? ??????, ??????????? ?????????
     */
    @Override
    public String toString() {
        return getFullNumber()
                + (getInput_data().isEmpty() ? "" : " " + getInput_data())
                + ((postponedStatus == null || postponedStatus.isEmpty()) ? ""
                        : " " + postponedStatus
                                + (postponPeriod > 0 ? " (" + postponPeriod + "min.)" : "")
                                + (isMine != null ? " Private!" : ""));
    }

    @Transient
    @Override
    public String getName() {
        return getFullNumber() + " " + getInput_data();
    }

    @Transient
    public LinkedList<LinkedList<LinkedList<Long>>> getComplexId() {
        return complexId;
    }

    public void setComplexId(LinkedList<LinkedList<LinkedList<Long>>> complexId) {
        this.complexId = complexId;
    }

    @Transient
    public Integer getWaitingMinutes() {
        return new Long((System.currentTimeMillis() - getStandTime().getTime()) / 1000 / 60 + 1)
                .intValue();
    }

    @Column(name = "service_quantity")
    public String getQuantity() {
        //        QLog.l().logger().trace("\nNAME:  " + this.getService().getName() + "\n ");
        //        QLog.l().logger().trace("\nTTEST from Customer:  " + quantity + " \n ");
        //        return this.getService().getQuantity();
        return this.quantity;
    }

    public void setQuantity(String quantity) {
        //        QLog.l().logger().trace("/n/nTTTTTTTEST666:  " + quantity + " \n\n ");
        //        this.getService().setQuantity(quantity);
        this.quantity = quantity;
    }

    public void refreshQuantity() {
        //        customer = user.getUser().getCustomer();
        this.setQuantity("1");
    }

    @Column(name = "channels")
    public String getChannels() {
        //        QLog.l().logger().trace("\n\n\n\n CHANNEL NAME:\n  " + channels + "\n\n ");
        //        return this.getService().getQuantity();
        return this.channels;
    }

    public void setChannels(String c) {
        this.channels = c;
    }

    @Column(name = "channelsIndex")
    public int getChannelsIndex() {
        return this.channelsIndex;
    }

    public void setChannelsIndex(int c) {
        this.channelsIndex = c;
    }

    ;

    @Column(name = "previous_service", updatable = false, insertable = false)
    public LinkedList<QService> getPreviousList() {
        return this.PreviousList;
    }

    public void setPreviousList(QService s) {
        if (!PreviousList.contains(s)) {
            PreviousList.add(s);
        }
    }

    public void refreshPrevious() {
        this.PreviousList = null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_id")
    public QOffice getOffice() {
        return office;
    }

    public void setOffice(QOffice office) {
        this.office = office;
    }

    @Transient
    public Boolean getIsInSequence() {
        return this.in_sequence;
    }

    public void setIsInSequence(Boolean isInSequence) {
        this.in_sequence = isInSequence;
    }

    @Transient
    public Boolean getLogWaitQueue() {
        return this.log_waitqueue;
    }

    public void setLogWaitQueue(Boolean logQueue) {
        this.log_waitqueue = logQueue;
    }

    //@Column(name = "spId")
    @Transient
    public Long getSpId() {
        return this.spId;
    }

    public void setSpId(Long snowplowId) {
        this.spId = snowplowId;
    }

    @Transient
    public Integer getStateInPrevious() {
        return this.stateInPrevious;
    }

    public void setStateInPrevious(Integer stateValue) {
        this.stateInPrevious = stateValue;
    }
}