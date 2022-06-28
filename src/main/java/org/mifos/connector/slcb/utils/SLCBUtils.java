package org.mifos.connector.slcb.utils;

import org.mifos.connector.common.gsma.dto.GSMATransaction;
import org.mifos.connector.common.gsma.dto.GsmaParty;
import org.mifos.connector.slcb.dto.Payee;
import org.mifos.connector.slcb.dto.PaymentRequestDTO;

import java.util.ArrayList;
import java.util.List;

public class SLCBUtils {

    public static List<GSMATransaction> convertPaymentRequestDto(PaymentRequestDTO paymentRequestDTO) {
        List<GSMATransaction> GSMATransactions = new ArrayList<>();

        GsmaParty[] debitParty = new GsmaParty[2];
        debitParty[0] = new GsmaParty();
        debitParty[1] = new GsmaParty();

        debitParty[0].setKey("Account Number");
        debitParty[0].setValue(paymentRequestDTO.getSourceAccount());

        debitParty[1].setKey("Account Type");
        debitParty[1].setValue(""+paymentRequestDTO.getAccountType());

        for(Payee payee: paymentRequestDTO.getPayees()) {
            GSMATransaction transaction = convertPayeeToGSMATransaction(payee);

            transaction.setDebitParty(debitParty);
            transaction.setRequestDate(paymentRequestDTO.getRequestDate().toString());
            GSMATransactions.add(transaction);
        }

        return GSMATransactions;
    }

    private static GSMATransaction convertPayeeToGSMATransaction(Payee SLCBPayee) {
        GsmaParty[] creditParty = new GsmaParty[2];
        creditParty[0] = new GsmaParty();
        creditParty[1] = new GsmaParty();

        creditParty[0].setKey("Account Number");
        creditParty[0].setValue(SLCBPayee.getAccount());

        creditParty[1].setKey("Account Type");
        creditParty[1].setValue(""+SLCBPayee.getAccountType());


        GSMATransaction gsmaTransaction = new GSMATransaction();
        gsmaTransaction.setDescriptionText(SLCBPayee.getPurpose());
        gsmaTransaction.setTransactionReference(SLCBPayee.getTransactionId());
        gsmaTransaction.setTransactionStatus(SLCBPayee.getStatusMessage());
        gsmaTransaction.setAmount(""+SLCBPayee.getAmount());
        gsmaTransaction.setCreditParty(creditParty);
        return gsmaTransaction;
    }

}