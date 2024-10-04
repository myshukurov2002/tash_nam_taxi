package com.company.components;

public interface Components {

    //USER
    String ASK_FULL_NAME = "\uD83D\uDCDD Ismingizni kiriting! \nMisol: Salohiddin";
    String ASK_CONTACT_REQUEST = "☎️ Raqamizni jo'nating!";
    String TAXIST = "\uD83D\uDE96Haydovchi";
    String CLIENT = "\uD83E\uDD35Yo'lovchi";
    String ASK_USER_TYPE = "\uD83D\uDE96 or \uD83E\uDD35 \nHaydovchimisiz yoki Yo'lovchi?";

    //NEUTRAL
    String WRONG_ANSWER = "⁉️ Xato malumot kirittingiz! \nQaytadan urinib ko'ring!";
    String SUCCESS = "✅ Muvaffaqiyatli amalga oshirildi!";
    String FILE_NOT_FOUND = "\uD83E\uDD37\uD83C\uDFFB\u200D♀\uFE0F Image not found!";
    String ERROR_READING_IMAGE = "\uD83D\uDE2C Error reading image file!";
    String ERROR_SEND_PHOTO = "\uD83D\uDE2C Failed to send photo!";

    String MENU_BUTTON = "⚙️ Menyuga o'tish ";
    String OVERVIEW = "\uD83D\uDC47\uD83C\uDFFC Tugmani tanlang!";


    //TAXI
    String TAXI_START = "Siz botimizga '\uD83D\uDE96Haydovchi' sifatida kirdingiz! ✅";
    String CAR_PHOTO = "\uD83D\uDE96 Mashinangiz Rasmini raqami ko'rinadigan tarafdan rasm olib botga jo'nating!\nMisol: \uD83D\uDC47\uD83C\uDFFC";
    String ASK_ADDRESSES = "\uD83D\uDCCD O'zingiz doimiy qatnaydigan 3ta yo'nalishni tanlang! \nChunki tanlagan yo'nalishlarizga qarab sizga yulovchilar malumotlari jo'natiladi! \uD83D\uDC69\uD83C\uDFFC\u200D\uD83D\uDCBB";
    String CAR_IMG_PATH = "src/main/resources/static/img.png";
    String BOT_IMG_PATH = "src/main/resources/static/bot_ads.png";
    String ADDRESS_LIMIT = "✅ 3ta Shaharni tanlab bo'ldingiz. 🟢 Keyingi ni bosing!";
    String HELP = "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBBAdmin bilan bog'laning: @taksi_admin24\nDavom etish uchun /start ni bosing!";
    String SOON = "\uD83D\uDD27 Tez orada bu funksiya qo'shiladi! Tushunganingiz uchun rahmat! \uD83D\uDE42\nDavom etish uchun /start ni bosing!";
    String NOT_AVAILABLE = "\uD83D\uDDD2 Siz oldin ro'yxatdan o'tishingiz kerak!\nDavom etish uchun /start ni bosing!";
    String START = "\nDavom etish uchun /menu ni bosing!";
    String INCORRECT_PHOTO = "❌ Rasm talabga javob bermaydi!";
    String SHOULD_FILL = "❗\uFE0F Bu malumotlarni to'ldirmasdan keyingi qadamga o'ta olmaysiz!";
    String AFTER_APPROVE = "Akkauntingiz 30kun davomida Faol holatda ishlaydi ❗\uFE0F ";
    String ILL_GET = "\uD83D\uDE96 ZAKAS OLISH \uD83D\uDE96";
    String ILL_GET_BACK = "ZAKAS";

    //GROUP
    String GROUP_ADS = "ZAKASLAR FAQAT BOT ORQALI QABUL QILINADI!!! \n\n Shafyor e'lonlari faqat bot orqali beriladi!";
    String GROUP_ADS2 = "Xabaringiz Operatorga Yuborildi ✅\n Agar 30 sekund ichida javob kelmasa Tez va Ishonchli Botimiz orqali Taksist Chaqiring \uD83D\uDC47\uD83C\uDFFB";
    String GROUP_LINK = "\uD83D\uDE96 TAKSI CHAQIRISH \uD83D\uDE96";
    String GROUP_TAXI = "\uD83D\uDE96 TAKSI SIFATIDA ISHLASH \uD83D\uDE96";
    String SELECT_ = "SELECT_";
    String RESET_BUTTON = "\uD83D\uDD04";
    String FINISH_BUTTON = "\uD83C\uDFC1 Tugatish";
    String NEXT_BUTTON = "🟢 Keyingi";
    String FINISHED_ADDRESS = "✅ Sizda doimiy qatnaydigan 3ta Shahar Muvaffaqiyatli saqlandi.";// O'zingiz istagan vaqtda bu shaharlarni o'zgartira olishingiz mumkin!
    String REQUEST_ADMIN = "\n\uD83E\uDDD1\uD83C\uDFFB\u200D\uD83D\uDCBB So'rovingiz Moderatorga yuborildi! Akkauntingiz tasdiqlanishi uchun to'lov amalga oshirishingiz kerak! Buning uchun \nAdminga yozing: ";
    String PROFILE_INFO = "\uD83E\uDD35\uD83C\uDFFB Profilim";
    String CONNECT_ADMIN = "\uD83E\uDDD1\uD83C\uDFFB\u200D\uD83D\uDCBB Admin bilan bog'lanish";
    String NO_SAME_REGION = "❗️ Bir xil Shahar tanlash mumkin emas";
    String NOT_ADDRESS_LIMIT = "\uD83E\uDD37\uD83C\uDFFB\u200D♀\uFE0F Hali 3ta Shahar tanlamadingiz!";
    String CHAT_ID = "\uD83C\uDD94 Chat ID: ";
    String FULL_NAME = "\uD83D\uDC64 Ism: ";
    String PHONE = "\uD83D\uDCF2 Telefon: ";
    String ROADLESS = "\uD83D\uDCCD Qatnovlar: ";
    String NOT_ALLOWED = "\uD83C\uDF1F Status:\uD83D\uDD34 Faol emas! Admin javobini kuting!";
    String ALLOWED = "\uD83C\uDF1F Status:🟢 Faol holatda! \nSo'rovlar avtomatik sizga yo'naltiriladi \uD83D\uDE42";
    String USERNAME = "\uD83D\uDCE9 Username: @";
    String TAXI_MENU = "\uD83D\uDE96 Taksi Menyusi";
    String NOT_ALLOWED_2 = "Akkauntingiz tasdiqlangach  So'rovlar avtomatik sizga yo'naltiriladi \uD83D\uDE42";

    //DASHBOARD
    String MAIN_MENU = "\uD83D\uDD19 Bosh Menu";
    String APPROVE = "✅APPROVE";
    String CHECK_MARK = "✅";
    String DISAPPROVE = "❌DISAPPROVE";
    String X_MARK = "❌";
    Long ADMIN_ID = 7315968056L;
    Long SUPER_ADMIN_ID = 1174220995L;
    String APPROVED = "\uD83C\uDF89 Tabriklaymiz! Sizning akkauntingiz faol holatda.\nSizga quyidagi yo'nalishlarda Yo'lovchi so'rovlari taqdim etiladi!\n\n\n";
    String DISAPPROVED = "❌ Afsuski Biz Akkauntingizni tasdiqlay olmadik. Sabablar:\n\n1.Nikizda ismingiz yozilmagan bo'lishi mumkin.\n\n2.Mashinangizni rasmida Davlat Raqami ham tushkaniga etibor bering! Agar Raqam ko'rinmagan bo'lsa Arizani rad etilishiga sabab bo'ladi! \n\nEndi esa Haydovchi sifatida qaytadan ro'yhatdan o'ting";//Chunki siz quyidagi Xatolarga yo'l qo'ygan bo'lishingiz mumkin!1) Xato ism: <strike>fdfsg</strike> Zokir ✅
    String HELP_COMMAND = "Qo'shimcha yordam";
    String START_COMMAND = "Davom etish";
    String HELP_2 = "Botimizni qo'shimca afzalliklari: Agarda sizda Taksi guruhingiz bo'lsa Botni Guruhizga qo'shib Admin qilsangiz Bot Guruhizdagi yozishuvlar va ortiqcha narsalarni o'chirib turib sizga boshqarishga yordam bera oladi!";
    String ADD_GROUP = "\uD83D\uDC65 Guruhga Qo'shish";
    String GROUP_URL_START = "?startgroup=new";

    //CLIENT
    String CLIENT_ADDRESS = "\uD83D\uDCCD Qayerdan  Qayerga Bormoqchisiz?";

    String CANCEL_RIDING = "🏳️ Bekor qilish";
    String CANCEL_RIDING_BUTTON = "⬅\uFE0F Orqaga";
    String LOCATIONS = "\uD83D\uDCCD Manzil: ";
    String ADDRESSES_NOT_YET_2 = "\uD83E\uDD37\uD83C\uDFFB\u200D♀\uFE0F Hali Qayerdan Qayerga Borishizni To'lliq Tanlamadiz ❗\uFE0F";
    String CLEARED_ADDRESS = "\uD83E\uDDF9 Tozalandi";
    String CANCELED = "🏳️ Safar Bekor Qilindi";
    String VOYAGES = "\uD83D\uDE95 Tarix";
    String WAIT_TAXI = "\uD83D\uDE96 Taksist javobini kuting";
    String CLIENT_MENU = "\uD83D\uDE42 Menyu";
    String CALL_TAXI = "\uD83D\uDE96 Taxi Xizmati";

    String PEOPLE_OR_POSTAGE = "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67 Yo'lovchi yoki \uD83D\uDCE6 Pochta?";
    String SELECTED_2_REGIONS = "\uD83D\uDCCD Qayerdan Qayerga Borishizni kiritib bo'ldingiz. 🟢 Keyingini Tanlang";
    String A_POSTAGE = "\uD83D\uDCE6 Pochta bor";
    //    String ONE = "1\uFE0F⃣";
    //    String TWO = "2\uFE0F⃣";
    //    String THREE = "3\uFE0F⃣";
    //    String FOUR = "4\uFE0F⃣";
    String A_ONE = "1\uFE0F⃣ kishi bor";
    String A_TWO = "2\uFE0F⃣ kishi bor";
    String A_THREE = "3\uFE0F⃣ kishi bor";
    String A_FOUR = "4\uFE0F⃣ kishi bor";
    String PERSON_OR_POSTAGE = "Yo'lovchi yoki Pochta ❓";
    String YES = "✅ HA";
    String NO = "❌ Yo'q";
    String IS_CORRECT = "Barcha Malumotlar To'g'rimi?";
    String EXCEPTION_OCCURED = "⁉\uFE0F Xatolik Sodir bo'ldi! Iltimos Qaytdan Urinib Ko'ring!";
    String TELEGRAM_LINK = "https://t.me/";
    String FROM_TO = "\uD83C\uDFD6 Safar: Qayerdan Qayerga ❓";
    String SUCCESS_SENT = "\uD83D\uDC4C\uD83C\uDFFB Muvaffaqiyatli jo'natildi";
    String VOYAGE_ID = "\uD83C\uDD94 Safar id si: ";
    String ONLY_UZB = "\uD83E\uDD37\uD83C\uDFFB\u200D♂\uFE0F Bot faqat O'zbekiston hududidagi shaxslarga xizmat ko'rsatadi!";
    String NUMBER_NOT_YOURS = "Bu raqam egasi siz emassiz! \uD83E\uDEE4";

    String ONE_TWO = "Namangandan Toshkentga";
    String TWO_ONE = "Toshkentdan Namanganga";
    String TIME = "\uD83D\uDD54 Muddat: ";
    String WILL_ORDER = "Zakas olmochi:";
    String IS_AGREE = "Agarda kelisha olmasangiz guruhga klient raqami tashalishi so'raladi❗\uFE0F";
    String PL_INPUT = "Assalomu aleykum! Botimizdan foydalanish uchun so'ralgan malumotlarni to'ldirishingiz talab etiladi \uD83D\uDE42";
    String GIVE_ADD = "\uD83D\uDCCC E'LON BERISH";
    String TAXI_ADS_EXAMPLE = "Assalomu aleykum\n Namangandan Toshkentga\n soat 02:00da yuramiz\n 2kishi srochni kerak!\n...";
    String TAXI_ADS = "E'loningizni quyidagiga o'xshab to'ldirishingiz mumkin!";
    String BOT_ADS = "\uD83D\uDE96 Zakas Berish \uD83D\uDE96";
    String BE_TAXI = "\uD83D\uDE96 Taksi sifatida ishlash \uD83D\uDE96";
    String ATTENTION_ALL_TAXIST_1 = "SHAFYORLAR DIQQATIGA❗\uFE0F - E'lonlarni Bot Orqali Bering❗\uFE0F\n YO'LOVCHILAR DIQQATIGA❗\uFE0F - Barcha Turdagi Zakaslar Faqat Bot Orqali Qabul Qilinadi❗\uFE0F";
    String ATTENTION_ALL_TAXIST_2 = "\uFE0F\n YO'LOVCHILAR DIQQATIGA❗\uFE0F - Barcha Turdagi Zakaslar Faqat Bot Orqali Qabul Qilinadi❗\uFE0F";
    String ATTENTION_TAXIST = "❗\uFE0F E'LON BERISH faqat bot orqali amalga oshiriladi!";

    String CALL_BOT = "\uD83D\uDE96 E'LON BERISH \uD83D\uDE96";
}
