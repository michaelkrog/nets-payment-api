nets-payment-api
================

A Java API for authorizing and capturing payments via Nets/PBS. This project is a work in progress.
Any help is appreciated.

Merchant Guide can be acquired here: http://www.nets.eu/dk-da/Service/verifikation-af-betalingsloesninger/specifikationer-og-aftaler/krav-til-internetloesning/Pages/default.aspx
Or by mailing Nets: it-verification@nets.eu

Example of the current API state:
```Java
Repository<TransactionData, String> dataRep = ...;
ChannelFactory cf = new SslSocketChannelFactory("<host>", <port>);
Nets nets = new Nets(cf, dataRep);
Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
Card card = new Card("<card>", 12, 12, "123");
Money money = Money.of(CurrencyUnit.USD, 199.95);
String orderId = "<orderid>";

try{
    nets.authorize(merchant, card, money, orderId);
    nets.capture(merchant, money, orderId, refund);
} catch(NetsException ex) {
    LOG.error("Not approved. [action={};merchantAction={}]", ex.getAction(), ex.getAction().getMerchantAction());
}
```