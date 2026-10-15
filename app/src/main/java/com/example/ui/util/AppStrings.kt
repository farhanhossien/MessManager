package com.example.ui.util

object AppStrings {
    fun t(key: String, isBangla: Boolean = true): String {
        return if (isBangla) banglaStrings[key] ?: key else englishStrings[key] ?: key
    }

    fun formatTaka(amount: Double): String {
        return "৳" + String.format(java.util.Locale.US, "%,.2f", amount)
    }

    fun formatMeal(count: Float): String {
        return if (count % 1.0f == 0.0f) {
            count.toInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", count)
        }
    }

    private val banglaStrings = mapOf(
        "app_name" to "মেস ম্যানেজার",
        "tagline" to "ব্যাচেলর মেস ও হোস্টেলের নির্ভুল মিল হিসাব",
        "nav_dashboard" to "ড্যাশবোর্ড",
        "nav_meals" to "মিল এন্ট্রি",
        "nav_bazar" to "বাজার",
        "nav_deposits" to "জমা",
        "nav_summary" to "হিসাব",
        "nav_members" to "সদস্য",
        "nav_reports" to "রিপোর্ট",
        "nav_settings" to "সেটিংস",
        "nav_fixed_costs" to "স্থায়ী খরচ",

        // Auth
        "login_title" to "লগইন করুন",
        "register_title" to "নতুন অ্যাকাউন্ট খুলুন",
        "phone_label" to "মোবাইল নম্বর",
        "phone_hint" to "০১৭xxxxxxxx বা +৮৮০...",
        "pin_label" to "পিন নম্বর (৪-৬ ডিজিট)",
        "pin_hint" to "••••",
        "name_label" to "আপনার নাম",
        "name_hint" to "যেমন: ফারহান আহমেদ",
        "btn_login" to "লগইন",
        "btn_register" to "রেজিস্টার করুন",
        "forgot_pin_hint" to "PIN ভুলে গেলে মেস অ্যাডমিনকে বলুন তাকে সরিয়ে নতুন করে যোগ করতে",
        "switch_to_register" to "নতুন ব্যবহারকারী? রেজিস্টার করুন",
        "switch_to_login" to "ইতিমধ্যে অ্যাকাউন্ট আছে? লগইন করুন",
        "logout" to "লগআউট",

        // Mess Selection
        "my_messes" to "আমার মেসসমূহ",
        "start_mess_title" to "নতুন মেস শুরু করুন",
        "mess_name_label" to "মেসের নাম",
        "mess_name_hint" to "যেমন: ধানমন্ডি ব্যাচেলর পয়েন্ট",
        "btn_create_mess" to "মেস তৈরি করুন",
        "join_mess_title" to "মেসে যুক্ত হোন",
        "invite_code_label" to "৬ অক্ষরের ইনভাইট কোড",
        "invite_code_hint" to "যেমন: MX4K7P",
        "btn_send_request" to "যুক্ত হওয়ার অনুরোধ পাঠান",
        "pending_request_banner" to "আপনার অনুরোধ অ্যাডমিনের অনুমোদনের অপেক্ষায় আছে। অনুমোদন পেলে সব তথ্য দেখতে পাবেন।",
        "no_messes_yet" to "আপনি এখনও কোনো মেসে যুক্ত হননি। নতুন মেস খুলুন অথবা ইনভাইট কোড দিয়ে যুক্ত হোন।",

        // Dashboard
        "month_label" to "চলতি মাস",
        "total_bazar" to "মোট বাজার খরচ",
        "total_meals" to "মোট মিল সংখ্যা",
        "current_meal_rate" to "বর্তমান মিল রেট",
        "today_meals" to "আজকের মিল",
        "breakfast" to "সকাল",
        "lunch" to "দুপুর",
        "dinner" to "রাত",
        "guest" to "গেস্ট মিল",
        "quick_actions" to "দ্রুত অ্যাকশন",
        "add_bazar" to "বাজার খরচ যোগ",
        "add_deposit" to "টাকা জমা যোগ",
        "member_balances" to "সদস্যদের ব্যালেন্স সামারি",
        "will_receive" to "পাবে",
        "will_give" to "দিবে (বকেয়া)",
        "load_demo" to "ডেমো ডাটা লোড করুন (Test only)",
        "demo_loaded" to "ডেমো মেসের ডেটা সফলভাবে লোড হয়েছে!",

        // Meals Screen
        "meal_date" to "তারিখ",
        "guest_meals_label" to "গেস্ট মিল",
        "save_meals" to "মিল সেভ করুন",
        "tomorrow_toggle_title" to "আগামীকালের মিল বন্ধ/চালু",
        "cutoff_notice" to "কাটঅফ রাত ১০:০০ টা পর্যন্ত পরিবর্তনযোগ্য",
        "meal_updated" to "মিল আপডেট হয়েছে",
        "total_day_meals" to "এই দিনের মোট মিল",

        // Bazar Screen
        "bazar_list" to "বাজার খরচের তালিকা",
        "add_expense_title" to "নতুন বাজার খরচ যুক্ত করুন",
        "buyer_label" to "কে বাজার করেছেন?",
        "amount_label" to "টাকার পরিমাণ (৳)",
        "category_label" to "ক্যাটাগরি",
        "note_label" to "বিবরণ (ঐচ্ছিক)",
        "cat_rice" to "চাল",
        "cat_dal" to "ডাল",
        "cat_fish" to "মাছ",
        "cat_meat" to "মাংস",
        "cat_veg" to "শাকসবজি",
        "cat_spices" to "মসলা",
        "cat_oil" to "তেল",
        "cat_others" to "অন্যান্য",
        "no_expenses" to "এই মাসে এখনও কোনো বাজার খরচ নেই।",

        // Deposits
        "deposit_list" to "জমা টাকার তালিকা",
        "add_deposit_title" to "টাকা জমা করুন",
        "member_label" to "কোন সদস্য জমা দিয়েছেন?",
        "payment_method" to "পেমেন্ট মাধ্যম",
        "method_bkash" to "বিকাশ",
        "method_nagad" to "নগদ",
        "method_rocket" to "রকেট",
        "method_cash" to "ক্যাশ / নগদ হাতে",
        "method_bank" to "ব্যাংক",
        "no_deposits" to "এই মাসে কোনো টাকা জমা নেই।",

        // Fixed Costs
        "fixed_cost_title" to "স্থায়ী / শেয়ার্ড খরচ",
        "add_fixed_cost" to "স্থায়ী খরচ যোগ করুন",
        "title_label" to "খরচের নাম",
        "cat_maid" to "খালা বিল / বুয়া বিল",
        "cat_gas" to "গ্যাস বিল",
        "cat_electricity" to "বিদ্যুৎ বিল",
        "cat_wifi" to "ওয়াইফাই বিল",
        "cat_rent" to "বাসা ভাড়া",
        "cat_water" to "পানি/ময়লা বিল",
        "per_member_share" to "সদস্য প্রতি শেয়ার",

        // Members & Requests
        "members_title" to "মেস সদস্যবৃন্দ",
        "pending_requests" to "অনুরোধ",
        "btn_approve" to "অনুমোদন",
        "btn_reject" to "বাতিল",
        "add_managed_member" to "সরাসরি সদস্য যোগ (লগইন ছাড়া)",
        "managed_member_desc" to "যাদের স্মার্টফোন বা অ্যাকাউন্ট নেই, তাদের নাম দিয়ে অ্যাডমিন নিজেই হিসাব রাখবেন।",
        "managed_badge" to "ম্যানেজড",
        "admin_badge" to "অ্যাডমিন",
        "manager_badge" to "ম্যানেজার",
        "member_badge" to "মেম্বার",
        "invite_code_banner" to "মেসের ইনভাইট কোড",
        "btn_copy" to "কপি",
        "btn_share" to "শেয়ার (WhatsApp)",
        "btn_regen_code" to "নতুন কোড তৈরি করুন",
        "reset_pin_title" to "পিন রিসেট করুন",
        "new_pin_label" to "নতুন পিন",
        "promote_manager" to "ম্যানেজার বানান",
        "demote_member" to "সাধারণ মেম্বার বানান",
        "remove_member" to "মেস থেকে সরান",

        // Monthly Summary & Close
        "monthly_summary_title" to "মাসিক চূড়ান্ত হিসাব",
        "close_month_btn" to "চলতি মাস ক্লোজ করুন",
        "close_month_confirm" to "আপনি কি নিশ্চিত চলতি মাস ক্লোজ করতে চান? ক্লোজ করলে মাসের হিসাব লক হয়ে যাবে এবং ব্যালেন্স পরবর্তী মাসে ক্যারি-ফরওয়ার্ড হবে।",
        "reopen_month_btn" to "মাস আনলক করুন",
        "member_name" to "সদস্য",
        "meals_col" to "মিল",
        "meal_cost_col" to "মিল খরচ",
        "fixed_col" to "স্থায়ী খরচ",
        "total_cost_col" to "মোট খরচ",
        "deposit_col" to "জমা",
        "balance_col" to "ব্যালেন্স",

        // Reports
        "reports_title" to "রিপোর্ট ও চার্ট",
        "share_summary" to "হিসাব শেয়ার করুন (WhatsApp / SMS)",
        "export_csv" to "CSV ফাইল এক্সপোর্ট",
        "bazar_trend" to "দৈনিক বাজার খরচের গ্রাফ",
        "category_share" to "ক্যাটাগরি ভিত্তিক বাজার বন্টন",

        // Settings
        "settings_title" to "সেটিংস",
        "language_toggle" to "ভাষা পরিবর্তন (Language)",
        "dark_mode" to "ডার্ক মোড",
        "mess_info" to "মেসের তথ্য",
        "current_user_info" to "বর্তমান ইউজার",
        "cutoff_setting" to "আগামীকালের মিল কাটঅফ সময়"
    )

    private val englishStrings = mapOf(
        "app_name" to "Meal Manager",
        "tagline" to "Precise Meal & Bazar Manager for Hostels & Messes",
        "nav_dashboard" to "Dashboard",
        "nav_meals" to "Meal Entry",
        "nav_bazar" to "Bazar",
        "nav_deposits" to "Deposits",
        "nav_summary" to "Summary",
        "nav_members" to "Members",
        "nav_reports" to "Reports",
        "nav_settings" to "Settings",
        "nav_fixed_costs" to "Fixed Costs",

        // Auth
        "login_title" to "Sign In",
        "register_title" to "Create Account",
        "phone_label" to "Phone Number",
        "phone_hint" to "017xxxxxxxx or +880...",
        "pin_label" to "4-6 Digit PIN",
        "pin_hint" to "••••",
        "name_label" to "Your Name",
        "name_hint" to "e.g. Farhan Ahmed",
        "btn_login" to "Sign In",
        "btn_register" to "Register",
        "forgot_pin_hint" to "Forgot PIN? Ask your mess admin to remove and re-add you.",
        "switch_to_register" to "New user? Create account",
        "switch_to_login" to "Already have an account? Sign in",
        "logout" to "Log Out",

        // Mess Selection
        "my_messes" to "My Messes",
        "start_mess_title" to "Start a New Mess",
        "mess_name_label" to "Mess Name",
        "mess_name_hint" to "e.g. Dhanmondi Bachelor Point",
        "btn_create_mess" to "Create Mess",
        "join_mess_title" to "Join a Mess",
        "invite_code_label" to "6-Character Invite Code",
        "invite_code_hint" to "e.g. MX4K7P",
        "btn_send_request" to "Send Join Request",
        "pending_request_banner" to "Your join request is pending approval from the mess admin.",
        "no_messes_yet" to "You have not joined any mess yet. Create a new one or join with an invite code.",

        // Dashboard
        "month_label" to "Current Month",
        "total_bazar" to "Total Bazar Cost",
        "total_meals" to "Total Meals",
        "current_meal_rate" to "Current Meal Rate",
        "today_meals" to "Today's Meals",
        "breakfast" to "Breakfast",
        "lunch" to "Lunch",
        "dinner" to "Dinner",
        "guest" to "Guest Meals",
        "quick_actions" to "Quick Actions",
        "add_bazar" to "Add Bazar",
        "add_deposit" to "Add Deposit",
        "member_balances" to "Member Balances",
        "will_receive" to "Receivable",
        "will_give" to "Due",
        "load_demo" to "Load demo data (Test only)",
        "demo_loaded" to "Demo Mess loaded successfully!",

        // Meals Screen
        "meal_date" to "Date",
        "guest_meals_label" to "Guest Meals",
        "save_meals" to "Save Meals",
        "tomorrow_toggle_title" to "Tomorrow's Meal Toggle",
        "cutoff_notice" to "Cutoff is 10:00 PM for tomorrow's meals",
        "meal_updated" to "Meals updated",
        "total_day_meals" to "Total Meals for this Day",

        // Bazar Screen
        "bazar_list" to "Bazar Expense List",
        "add_expense_title" to "Add Bazar Expense",
        "buyer_label" to "Who purchased?",
        "amount_label" to "Amount (৳)",
        "category_label" to "Category",
        "note_label" to "Notes (Optional)",
        "cat_rice" to "Rice",
        "cat_dal" to "Lentils (Dal)",
        "cat_fish" to "Fish",
        "cat_meat" to "Meat",
        "cat_veg" to "Vegetables",
        "cat_spices" to "Spices",
        "cat_oil" to "Oil",
        "cat_others" to "Others",
        "no_expenses" to "No bazar expenses logged for this month yet.",

        // Deposits
        "deposit_list" to "Deposit List",
        "add_deposit_title" to "Add Deposit",
        "member_label" to "Member",
        "payment_method" to "Payment Method",
        "method_bkash" to "bKash",
        "method_nagad" to "Nagad",
        "method_rocket" to "Rocket",
        "method_cash" to "Cash in Hand",
        "method_bank" to "Bank Transfer",
        "no_deposits" to "No deposits logged for this month.",

        // Fixed Costs
        "fixed_cost_title" to "Fixed & Shared Costs",
        "add_fixed_cost" to "Add Shared Cost",
        "title_label" to "Cost Title",
        "cat_maid" to "Cook / Maid Bill",
        "cat_gas" to "Gas Bill",
        "cat_electricity" to "Electricity Bill",
        "cat_wifi" to "WiFi Bill",
        "cat_rent" to "House Rent",
        "cat_water" to "Water & Waste",
        "per_member_share" to "Per Member Share",

        // Members & Requests
        "members_title" to "Mess Members",
        "pending_requests" to "Requests",
        "btn_approve" to "Approve",
        "btn_reject" to "Reject",
        "add_managed_member" to "Add Managed Member (No login required)",
        "managed_member_desc" to "For roommates without a smartphone or account. Admin manages their meals and records.",
        "managed_badge" to "Managed",
        "admin_badge" to "Admin",
        "manager_badge" to "Manager",
        "member_badge" to "Member",
        "invite_code_banner" to "Mess Invite Code",
        "btn_copy" to "Copy",
        "btn_share" to "Share (WhatsApp)",
        "btn_regen_code" to "Regenerate Code",
        "reset_pin_title" to "Reset PIN",
        "new_pin_label" to "New PIN",
        "promote_manager" to "Promote to Manager",
        "demote_member" to "Demote to Member",
        "remove_member" to "Remove from Mess",

        // Monthly Summary & Close
        "monthly_summary_title" to "Monthly Final Statement",
        "close_month_btn" to "Close This Month",
        "close_month_confirm" to "Are you sure you want to close this month? This will lock the records and carry forward all net balances to next month.",
        "reopen_month_btn" to "Unlock Month",
        "member_name" to "Member",
        "meals_col" to "Meals",
        "meal_cost_col" to "Meal Cost",
        "fixed_col" to "Fixed Cost",
        "total_cost_col" to "Total Cost",
        "deposit_col" to "Deposit",
        "balance_col" to "Balance",

        // Reports
        "reports_title" to "Reports & Analytics",
        "share_summary" to "Share Statement (WhatsApp / SMS)",
        "export_csv" to "Export CSV",
        "bazar_trend" to "Daily Bazar Spending Trend",
        "category_share" to "Bazar Category Distribution",

        // Settings
        "settings_title" to "Settings",
        "language_toggle" to "Change Language",
        "dark_mode" to "Dark Mode",
        "mess_info" to "Mess Information",
        "current_user_info" to "Logged-in User",
        "cutoff_setting" to "Tomorrow Meal Cutoff Time"
    )
}
