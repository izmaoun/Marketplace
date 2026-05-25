package org.sid.payment_service.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.sid.payment_service.config.StripeProperties;
import org.sid.payment_service.dto.CreatePaymentRequest;
import org.sid.payment_service.dto.PaymentResponse;
import org.sid.payment_service.entity.Payment;
import org.sid.payment_service.entity.PaymentStatus;
import org.sid.payment_service.entity.StripeAccount;
import org.sid.payment_service.entity.StripeAccountOwnerType;
import org.sid.payment_service.repository.PaymentRepository;
import org.sid.payment_service.repository.StripeAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class StripePaymentService {
    private final PaymentRepository paymentRepository;
    private final StripeAccountRepository stripeAccountRepository;
    private final StripeProperties stripeProperties;
    private final PaymentFeeCalculator feeCalculator;

    public StripePaymentService(PaymentRepository paymentRepository,
                                StripeAccountRepository stripeAccountRepository,
                                StripeProperties stripeProperties,
                                PaymentFeeCalculator feeCalculator) {
        this.paymentRepository = paymentRepository;
        this.stripeAccountRepository = stripeAccountRepository;
        this.stripeProperties = stripeProperties;
        this.feeCalculator = feeCalculator;
    }

    @Transactional
    public PaymentResponse createPaymentForMissionComplete(String missionId, CreatePaymentRequest request) throws StripeException {
        StripeAccount stripeAccount = stripeAccountRepository
                .findByOwnerTypeAndOwnerId(StripeAccountOwnerType.FREELANCER, request.getFreelancerId())
                .orElseThrow(() -> new IllegalStateException("Freelancer stripe account not found"));

        long amountCents = request.getAmountCents();
        long feeCents = feeCalculator.calculateFee(amountCents, stripeProperties.getPlatformFeePercent());
        long netCents = Math.max(0, amountCents - feeCents);

        Payment payment = new Payment();
        payment.setMissionId(missionId);
        payment.setCompanyId(request.getCompanyId());
        payment.setFreelancerId(request.getFreelancerId());
        payment.setAmountCents(amountCents);
        payment.setCurrency(resolveCurrency(request.getCurrency()));
        payment.setPlatformFeeCents(feeCents);
        payment.setNetAmountCents(netCents);
        payment.setStatus(PaymentStatus.CREATED);
        paymentRepository.save(payment);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(payment.getCurrency())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .setApplicationFeeAmount(feeCents)
                .setTransferData(
                        PaymentIntentCreateParams.TransferData.builder()
                                .setDestination(stripeAccount.getStripeAccountId())
                                .build()
                )
                .putMetadata("missionId", missionId)
                .putMetadata("companyId", request.getCompanyId())
                .putMetadata("freelancerId", request.getFreelancerId())
                .putMetadata("paymentId", payment.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        payment.setStripePaymentIntentId(intent.getId());
        payment.setStripeClientSecret(intent.getClientSecret());
        payment.setStatus(PaymentStatus.REQUIRES_PAYMENT);
        paymentRepository.save(payment);

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        return toResponse(payment);
    }

    @Transactional
    public void markSucceeded(String paymentIntentId) {
        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);
        });
    }

    @Transactional
    public void markFailed(String paymentIntentId) {
        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        });
    }

    private String resolveCurrency(String requestedCurrency) {
        if (requestedCurrency == null || requestedCurrency.isBlank()) {
            return stripeProperties.getDefaultCurrency();
        }
        return requestedCurrency.toLowerCase(Locale.ROOT);
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId().toString());
        response.setStatus(payment.getStatus().name());
        response.setClientSecret(payment.getStripeClientSecret());
        response.setAmountCents(payment.getAmountCents());
        response.setPlatformFeeCents(payment.getPlatformFeeCents());
        response.setNetAmountCents(payment.getNetAmountCents());
        response.setCurrency(payment.getCurrency());
        response.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        return response;
    }
}

