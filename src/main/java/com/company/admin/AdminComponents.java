package com.company.admin;

public interface AdminComponents {

    String YOUR_BANNED = "Afsuski Taxi Gruppadan Siz BAN oldingiz. Qayta Qo'shilish uchun adminga murojaat qiling:\n";
    String CAN_BANNED = "Siz 2 kundan kein Taxi gruppadan Ban olishingiz mumkin. Vaqtida admin bilan bog'laning:\n";
    String UNBANNED = "Tabriklaymiz! Siz blokdan chiqarildingiz! Guruhga qo'shilishingiz mumkin";
    String ENTER_THE_GROUP = "Taxi Guruhimizga qo'shiling!";
    String ADD_DURATION = "/add";
    String DURATION_EXTENDED = "Botdan foydalanish muddatingiz uzaytirildi.\nUmumiy: ";
    String JOIN_GROUP = "Barcha Klient Malumotlari mana shu guruhga tashlab boriladi. Guruhga qo'shiling \uD83D\uDC47\uD83C\uDFFC\n";
    String SHOULD_PAY = "Botdan Taksi sifatida foydalanish uchun to'lov amalga oshirishingiz kerak! Bunda to'lovdan kein akkauntingiz 30kun davomida faol holatda ishlaydi. Bonus sifatida 5kun qo'shib beriladi!";
    String TAXI_PRICE = "\uD83D\uDCB0 Narx: ";
    String CARD_NUMBER = "\uD83D\uDCB3 Karta Raqami: ";
    String CARD_OWNER = "\uD83E\uDDD1\uD83C\uDFFB\u200D\uD83D\uDCBB Karta Egasi: ";

    //COMMANDS
    String BAN = "/ban";
    String UNBAN = "/unban";
    String CARD = "/card";
    String SEND = "/send";
    String GET = "/get";
    String GET_ALL = "/get-all";
    String DELETE = "/delete";
    String LINK = "/link";
    String PROMOTE_ADMIN = "/admin";
    String STARTUP = "/startup";
    String RESTART = "/restart";
    String SHUTDOWN = "/shutdown";
    String RUN = "/run";
    String GET_ALL2 = "/all";
    String ADS = "/ads";
    String COMMANDS = """
            taksist haqida malumot olish
            <code>/get taxi_id</code>

            taksistni aktiv qilish va unga qanchadur kun qo'shish
            <code>/add kun_soni taxi_id</code>

            ban berish
            <code>/ban taxi_id</code>

            qaytadan taksistni guruhga qo'shish
            <code>/unban taxi_id</code>

            tulov haqida taksistni ogohlantirish
            <code>/card taxi_id</code>

            barcha taksistlar haqida malumot
            <code>/get-all</code>
            
            gruppaga botni reklamasini qadash
            <code>/ads 1</code>
            """;
    String ALL_USERS = "/all-users";
    String ALL_TAXI = "/all-taxi";
    String VOYAGES = "/voyages";
    String STATISTICS = "/statistics";
    String BAN_USER = "/ban-user";
    String EXECUTE = "/execute";
    String UNBAN_USER = "/unban-user";
    String GEt_USER = "/get-user";
}
