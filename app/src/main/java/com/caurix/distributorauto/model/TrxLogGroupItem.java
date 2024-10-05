package com.caurix.distributorauto.model;


import com.caurix.distributorauto.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class TrxLogGroupItem extends ExpandableGroup<TrxLog> {

private TrxLog trxLog;

    public TrxLogGroupItem(String name, String phone, List<TrxLog> items) {
        super(name,phone, items);
    }

    public TrxLog getTrxLog() {
        return trxLog;
    }

    public void setTrxLog(TrxLog trxLog) {
        this.trxLog = trxLog;
    }

}
