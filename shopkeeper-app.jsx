import { useState, useEffect, useRef } from "react";

const DB = {
  customers: [
    { id: 1, name: "Ramesh Kumar", phone: "9876543210" },
    { id: 2, name: "Suresh Patel", phone: "9823456789" },
    { id: 3, name: "Anita Sharma", phone: "9012345678" },
  ],
  transactions: [
    { id: 1, customerId: 1, type: "lend", item: "Rice", qty: 5, unit: "kg", price: 60, total: 300, date: "2026-03-01", note: "" },
    { id: 2, customerId: 1, type: "lend", item: "Dal", qty: 2, unit: "kg", price: 120, total: 240, date: "2026-03-03", note: "" },
    { id: 3, customerId: 1, type: "payment", amount: 200, date: "2026-03-05", note: "Cash" },
    { id: 4, customerId: 2, type: "lend", item: "Sugar", qty: 3, unit: "kg", price: 45, total: 135, date: "2026-03-04", note: "" },
    { id: 5, customerId: 2, type: "payment", amount: 50, date: "2026-03-06", note: "UPI" },
    { id: 6, customerId: 3, type: "lend", item: "Oil", qty: 1, unit: "L", price: 180, total: 180, date: "2026-03-07", note: "" },
  ],
};

const VOICE_EXAMPLES = [
  "Ramesh ne 5 kg rice liya 60 rupaye kilo",
  "Suresh ne 2 kg dal liya 120 rupaye",
  "Anita ne 1 litre oil liya 180 rupaye",
  "Ramesh ne 200 rupaye diye",
  "Suresh ne 135 rupaye diye cash mein",
  "New customer Vijay phone 9900112233",
];

const BILL_TEMPLATES = [
  { id: "simple", name: "Simple Bill", icon: "📄" },
  { id: "detailed", name: "Detailed Bill", icon: "📋" },
  { id: "fancy", name: "Fancy Bill", icon: "✨" },
];

function parseVoiceInput(text, customers) {
  const lower = text.toLowerCase();
  let result = { type: null, customerId: null, customerName: null, item: null, qty: null, unit: null, price: null, amount: null };

  // Find customer
  for (const c of customers) {
    if (lower.includes(c.name.toLowerCase().split(" ")[0].toLowerCase())) {
      result.customerId = c.id;
      result.customerName = c.name;
      break;
    }
  }

  // New customer
  const newCustMatch = lower.match(/new customer (\w+)(?: phone (\d+))?/i) || text.match(/नया customer (\w+)/i);
  if (newCustMatch) {
    result.type = "new_customer";
    result.customerName = newCustMatch[1];
    result.phone = newCustMatch[2] || "";
    return result;
  }

  // Payment keywords
  const payKeywords = ["diye", "paid", "payment", "rupaye diye", "de diye", "bheja"];
  if (payKeywords.some(k => lower.includes(k))) {
    result.type = "payment";
    const amtMatch = text.match(/(\d+)\s*rupaye/i) || text.match(/(\d+)\s*rs/i) || text.match(/(\d+)\s*₹/);
    if (amtMatch) result.amount = parseInt(amtMatch[1]);
    return result;
  }

  // Lend/item keywords
  const lendKeywords = ["liya", "le gaya", "leke gaya", "dena", "gave", "taken", "ne liya"];
  if (lendKeywords.some(k => lower.includes(k)) || result.customerId) {
    result.type = "lend";

    const items = ["rice", "dal", "sugar", "oil", "wheat", "flour", "salt", "atta", "maida", "besan", "poha", "tea", "milk", "ghee", "soap", "biscuit"];
    for (const item of items) {
      if (lower.includes(item)) { result.item = item.charAt(0).toUpperCase() + item.slice(1); break; }
    }

    const qtyMatch = text.match(/(\d+(?:\.\d+)?)\s*(kg|gm|litre|liter|l|piece|pcs|dozen|pkt|packet)/i);
    if (qtyMatch) { result.qty = parseFloat(qtyMatch[1]); result.unit = qtyMatch[2].toLowerCase(); }

    const priceMatch = text.match(/(\d+)\s*rupaye\s*(?:kilo|kg|litre|per|each)?/i) || text.match(/(\d+)\s*rs\s*(?:kilo|per)?/i);
    if (priceMatch) result.price = parseInt(priceMatch[1]);

    return result;
  }

  return result;
}

function getOutstanding(customerId, transactions) {
  let total = 0;
  for (const t of transactions) {
    if (t.customerId !== customerId) continue;
    if (t.type === "lend") total += t.total;
    if (t.type === "payment") total -= t.amount;
  }
  return total;
}

function formatDate(d) {
  return new Date(d).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
}

function BillModal({ customer, transactions, template, onClose }) {
  const items = transactions.filter(t => t.customerId === customer.id && t.type === "lend");
  const payments = transactions.filter(t => t.customerId === customer.id && t.type === "payment");
  const outstanding = getOutstanding(customer.id, transactions);

  return (
    <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.7)", zIndex: 100, display: "flex", alignItems: "center", justifyContent: "center", padding: 16 }}>
      <div style={{ background: "#fff", borderRadius: 16, width: "100%", maxWidth: 420, maxHeight: "90vh", overflow: "auto", padding: 24, fontFamily: "'Courier New', monospace" }}>
        {template === "fancy" && (
          <div style={{ textAlign: "center", borderBottom: "2px dashed #e2b96f", paddingBottom: 16, marginBottom: 16 }}>
            <div style={{ fontSize: 28 }}>🏪</div>
            <div style={{ fontSize: 20, fontWeight: 700, color: "#b45309" }}>SHREE RAM STORES</div>
            <div style={{ fontSize: 11, color: "#888" }}>Main Bazar, Pune • 9876543210</div>
            <div style={{ fontSize: 11, color: "#888", marginTop: 4 }}>GSTIN: 27XXXXX1234Z5</div>
          </div>
        )}
        {template !== "fancy" && (
          <div style={{ textAlign: "center", borderBottom: "2px dashed #333", paddingBottom: 12, marginBottom: 12 }}>
            <div style={{ fontSize: 16, fontWeight: 700 }}>SHREE RAM STORES</div>
            <div style={{ fontSize: 11, color: "#555" }}>Main Bazar, Pune</div>
          </div>
        )}

        <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, marginBottom: 12 }}>
          <div><b>Customer:</b> {customer.name}<br /><span style={{ color: "#666" }}>{customer.phone}</span></div>
          <div style={{ textAlign: "right" }}><b>Date:</b> {formatDate(new Date())}<br /><span style={{ color: "#666" }}>Bill #{Math.floor(Math.random() * 9000 + 1000)}</span></div>
        </div>

        <table style={{ width: "100%", fontSize: 12, borderCollapse: "collapse", marginBottom: 12 }}>
          <thead>
            <tr style={{ borderBottom: "1px solid #333" }}>
              <th style={{ textAlign: "left", padding: "4px 0" }}>Item</th>
              <th style={{ textAlign: "right", padding: "4px 0" }}>Qty</th>
              <th style={{ textAlign: "right", padding: "4px 0" }}>Rate</th>
              <th style={{ textAlign: "right", padding: "4px 0" }}>Amt</th>
            </tr>
          </thead>
          <tbody>
            {items.map((t, i) => (
              <tr key={i} style={{ borderBottom: "1px dotted #ccc" }}>
                <td style={{ padding: "4px 0" }}>{t.item}</td>
                <td style={{ textAlign: "right", padding: "4px 0" }}>{t.qty} {t.unit}</td>
                <td style={{ textAlign: "right", padding: "4px 0" }}>₹{t.price}</td>
                <td style={{ textAlign: "right", padding: "4px 0" }}>₹{t.total}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div style={{ borderTop: "1px solid #333", paddingTop: 8, fontSize: 12 }}>
          <div style={{ display: "flex", justifyContent: "space-between" }}><span>Total Billed:</span><span>₹{items.reduce((s, t) => s + t.total, 0)}</span></div>
          <div style={{ display: "flex", justifyContent: "space-between", color: "#16a34a" }}><span>Total Paid:</span><span>₹{payments.reduce((s, t) => s + t.amount, 0)}</span></div>
          <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 14, marginTop: 4, color: outstanding > 0 ? "#dc2626" : "#16a34a", borderTop: "2px solid #333", paddingTop: 4 }}>
            <span>Outstanding:</span><span>₹{outstanding}</span>
          </div>
        </div>

        {template === "fancy" && (
          <div style={{ textAlign: "center", marginTop: 16, fontSize: 11, color: "#888", borderTop: "2px dashed #e2b96f", paddingTop: 12 }}>
            धन्यवाद! फिर पधारें 🙏<br />Thank you for your business!
          </div>
        )}

        <div style={{ display: "flex", gap: 8, marginTop: 16 }}>
          <button onClick={onClose} style={{ flex: 1, padding: "10px", background: "#f3f4f6", border: "none", borderRadius: 8, cursor: "pointer", fontWeight: 600 }}>Close</button>
          <button style={{ flex: 1, padding: "10px", background: "#16a34a", color: "#fff", border: "none", borderRadius: 8, cursor: "pointer", fontWeight: 600 }}>📤 Share</button>
        </div>
      </div>
    </div>
  );
}

export default function App() {
  const [db, setDb] = useState(DB);
  const [screen, setScreen] = useState("home"); // home | customer | voice | bill
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [isListening, setIsListening] = useState(false);
  const [voiceText, setVoiceText] = useState("");
  const [parsed, setParsed] = useState(null);
  const [voiceStatus, setVoiceStatus] = useState("idle");
  const [exampleIdx, setExampleIdx] = useState(0);
  const [billTemplate, setBillTemplate] = useState("simple");
  const [showBill, setShowBill] = useState(false);
  const [toast, setToast] = useState(null);
  const [addCustomerModal, setAddCustomerModal] = useState(false);
  const [newCustName, setNewCustName] = useState("");
  const [newCustPhone, setNewCustPhone] = useState("");
  const [paymentModal, setPaymentModal] = useState(false);
  const [payAmount, setPayAmount] = useState("");
  const [searchQ, setSearchQ] = useState("");
  const pulseRef = useRef(null);

  const showToast = (msg, type = "success") => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const simulateVoice = () => {
    const example = VOICE_EXAMPLES[exampleIdx % VOICE_EXAMPLES.length];
    setExampleIdx(i => i + 1);
    setIsListening(true);
    setVoiceStatus("listening");
    setVoiceText("");
    setParsed(null);

    let i = 0;
    const interval = setInterval(() => {
      setVoiceText(example.slice(0, i + 1));
      i++;
      if (i >= example.length) {
        clearInterval(interval);
        setVoiceStatus("processing");
        setTimeout(() => {
          const result = parseVoiceInput(example, db.customers);
          setParsed(result);
          setVoiceStatus("done");
          setIsListening(false);
        }, 800);
      }
    }, 40);
  };

  const confirmVoiceAction = () => {
    if (!parsed) return;
    const now = new Date().toISOString().slice(0, 10);
    const newDb = { ...db, customers: [...db.customers], transactions: [...db.transactions] };

    if (parsed.type === "new_customer") {
      const newId = Math.max(...newDb.customers.map(c => c.id)) + 1;
      newDb.customers.push({ id: newId, name: parsed.customerName, phone: parsed.phone || "" });
      showToast(`Customer ${parsed.customerName} added!`);
    } else if (parsed.type === "lend" && parsed.customerId) {
      const newId = Math.max(...newDb.transactions.map(t => t.id)) + 1;
      newDb.transactions.push({
        id: newId, customerId: parsed.customerId, type: "lend",
        item: parsed.item || "Item", qty: parsed.qty || 1, unit: parsed.unit || "pcs",
        price: parsed.price || 0, total: (parsed.qty || 1) * (parsed.price || 0), date: now, note: ""
      });
      showToast(`Saved! ${parsed.item} for ${parsed.customerName}`);
    } else if (parsed.type === "payment" && parsed.customerId) {
      const newId = Math.max(...newDb.transactions.map(t => t.id)) + 1;
      newDb.transactions.push({ id: newId, customerId: parsed.customerId, type: "payment", amount: parsed.amount || 0, date: now, note: "" });
      showToast(`Payment ₹${parsed.amount} recorded!`);
    }

    setDb(newDb);
    setParsed(null);
    setVoiceText("");
    setVoiceStatus("idle");
  };

  const addPayment = () => {
    if (!payAmount || !selectedCustomer) return;
    const now = new Date().toISOString().slice(0, 10);
    const newId = Math.max(...db.transactions.map(t => t.id)) + 1;
    setDb(d => ({ ...d, transactions: [...d.transactions, { id: newId, customerId: selectedCustomer.id, type: "payment", amount: parseInt(payAmount), date: now, note: "" }] }));
    showToast(`Payment ₹${payAmount} recorded!`);
    setPayAmount("");
    setPaymentModal(false);
  };

  const addCustomer = () => {
    if (!newCustName) return;
    const newId = Math.max(...db.customers.map(c => c.id)) + 1;
    setDb(d => ({ ...d, customers: [...d.customers, { id: newId, name: newCustName, phone: newCustPhone }] }));
    showToast("Customer added!");
    setNewCustName(""); setNewCustPhone(""); setAddCustomerModal(false);
  };

  const filteredCustomers = db.customers.filter(c =>
    c.name.toLowerCase().includes(searchQ.toLowerCase()) || c.phone.includes(searchQ)
  );

  const totalOutstanding = db.customers.reduce((s, c) => s + Math.max(0, getOutstanding(c.id, db.transactions)), 0);

  // SCREENS
  const HomeScreen = () => (
    <div style={{ flex: 1, overflowY: "auto" }}>
      {/* Header Stats */}
      <div style={{ background: "linear-gradient(135deg, #1e3a5f 0%, #0f2540 100%)", padding: "20px 16px 28px", color: "#fff" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
          <div>
            <div style={{ fontSize: 13, opacity: 0.7, fontFamily: "Noto Sans, sans-serif" }}>🏪 Shree Ram Stores</div>
            <div style={{ fontSize: 22, fontWeight: 700, marginTop: 2 }}>Namaste! 🙏</div>
          </div>
          <div style={{ background: "rgba(255,255,255,0.1)", borderRadius: 10, padding: "6px 12px", textAlign: "center" }}>
            <div style={{ fontSize: 11, opacity: 0.7 }}>Today</div>
            <div style={{ fontSize: 13, fontWeight: 700 }}>{new Date().toLocaleDateString("en-IN", { day: "2-digit", month: "short" })}</div>
          </div>
        </div>
        <div style={{ marginTop: 20, background: "rgba(255,255,255,0.08)", borderRadius: 14, padding: "16px", backdropFilter: "blur(10px)" }}>
          <div style={{ fontSize: 12, opacity: 0.7 }}>Total Outstanding</div>
          <div style={{ fontSize: 32, fontWeight: 800, color: "#fbbf24" }}>₹{totalOutstanding.toLocaleString("en-IN")}</div>
          <div style={{ fontSize: 12, opacity: 0.6 }}>{db.customers.length} customers • {db.transactions.length} transactions</div>
        </div>
      </div>

      {/* Quick Actions */}
      <div style={{ padding: "16px 16px 0" }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: "#64748b", marginBottom: 10, letterSpacing: 0.5 }}>QUICK ACTIONS</div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}>
          {[
            { icon: "🎙️", label: "Voice Entry", sub: "Speak to record", action: () => setScreen("voice"), bg: "linear-gradient(135deg,#7c3aed,#4f46e5)" },
            { icon: "👥", label: "Customers", sub: `${db.customers.length} active`, action: () => setScreen("home"), bg: "linear-gradient(135deg,#0891b2,#0e7490)" },
            { icon: "📊", label: "Reports", sub: "View summary", action: () => {}, bg: "linear-gradient(135deg,#059669,#047857)" },
            { icon: "📄", label: "Generate Bill", sub: "Select & print", action: () => setScreen("bill"), bg: "linear-gradient(135deg,#d97706,#b45309)" },
          ].map((a, i) => (
            <button key={i} onClick={a.action} style={{ background: a.bg, border: "none", borderRadius: 14, padding: "16px 14px", textAlign: "left", cursor: "pointer", color: "#fff" }}>
              <div style={{ fontSize: 24, marginBottom: 6 }}>{a.icon}</div>
              <div style={{ fontSize: 14, fontWeight: 700 }}>{a.label}</div>
              <div style={{ fontSize: 11, opacity: 0.8, marginTop: 2 }}>{a.sub}</div>
            </button>
          ))}
        </div>
      </div>

      {/* Customer List */}
      <div style={{ padding: "16px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "#64748b", letterSpacing: 0.5 }}>CUSTOMERS</div>
          <button onClick={() => setAddCustomerModal(true)} style={{ background: "#1e3a5f", color: "#fff", border: "none", borderRadius: 8, padding: "5px 12px", fontSize: 12, cursor: "pointer" }}>+ Add</button>
        </div>
        <div style={{ background: "#f1f5f9", borderRadius: 10, padding: "8px 12px", display: "flex", alignItems: "center", gap: 8, marginBottom: 10 }}>
          <span style={{ fontSize: 14, color: "#94a3b8" }}>🔍</span>
          <input value={searchQ} onChange={e => setSearchQ(e.target.value)} placeholder="Search customers..." style={{ background: "none", border: "none", outline: "none", fontSize: 14, width: "100%", color: "#1e293b" }} />
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {filteredCustomers.map(c => {
            const out = getOutstanding(c.id, db.transactions);
            return (
              <button key={c.id} onClick={() => { setSelectedCustomer(c); setScreen("customer"); }} style={{ background: "#fff", border: "1px solid #e2e8f0", borderRadius: 12, padding: "12px 14px", display: "flex", alignItems: "center", justifyContent: "space-between", cursor: "pointer", textAlign: "left" }}>
                <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                  <div style={{ width: 40, height: 40, borderRadius: 20, background: `hsl(${c.id * 60},60%,85%)`, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16, fontWeight: 700, color: `hsl(${c.id * 60},60%,35%)` }}>
                    {c.name[0]}
                  </div>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, color: "#1e293b" }}>{c.name}</div>
                    <div style={{ fontSize: 12, color: "#94a3b8" }}>{c.phone || "No phone"}</div>
                  </div>
                </div>
                <div style={{ textAlign: "right" }}>
                  <div style={{ fontSize: 16, fontWeight: 700, color: out > 0 ? "#dc2626" : "#16a34a" }}>₹{out}</div>
                  <div style={{ fontSize: 11, color: "#94a3b8" }}>{out > 0 ? "due" : "clear"}</div>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );

  const CustomerScreen = () => {
    const c = selectedCustomer;
    if (!c) return null;
    const txns = db.transactions.filter(t => t.customerId === c.id).sort((a, b) => b.date.localeCompare(a.date));
    const out = getOutstanding(c.id, db.transactions);
    return (
      <div style={{ flex: 1, overflowY: "auto" }}>
        <div style={{ background: "linear-gradient(135deg,#1e3a5f,#0f2540)", padding: "16px 16px 24px", color: "#fff" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
            <div style={{ width: 52, height: 52, borderRadius: 26, background: `hsl(${c.id * 60},60%,85%)`, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 22, fontWeight: 700, color: `hsl(${c.id * 60},60%,35%)` }}>{c.name[0]}</div>
            <div>
              <div style={{ fontSize: 18, fontWeight: 700 }}>{c.name}</div>
              <div style={{ fontSize: 13, opacity: 0.7 }}>📞 {c.phone || "No phone"}</div>
            </div>
          </div>
          <div style={{ background: "rgba(255,255,255,0.08)", borderRadius: 12, padding: 14, display: "flex", justifyContent: "space-around" }}>
            <div style={{ textAlign: "center" }}>
              <div style={{ fontSize: 11, opacity: 0.7 }}>Total Billed</div>
              <div style={{ fontSize: 18, fontWeight: 700 }}>₹{txns.filter(t => t.type === "lend").reduce((s, t) => s + t.total, 0)}</div>
            </div>
            <div style={{ width: 1, background: "rgba(255,255,255,0.2)" }} />
            <div style={{ textAlign: "center" }}>
              <div style={{ fontSize: 11, opacity: 0.7 }}>Total Paid</div>
              <div style={{ fontSize: 18, fontWeight: 700, color: "#86efac" }}>₹{txns.filter(t => t.type === "payment").reduce((s, t) => s + t.amount, 0)}</div>
            </div>
            <div style={{ width: 1, background: "rgba(255,255,255,0.2)" }} />
            <div style={{ textAlign: "center" }}>
              <div style={{ fontSize: 11, opacity: 0.7 }}>Outstanding</div>
              <div style={{ fontSize: 18, fontWeight: 700, color: out > 0 ? "#fca5a5" : "#86efac" }}>₹{out}</div>
            </div>
          </div>
        </div>

        <div style={{ padding: "12px 16px", display: "flex", gap: 8 }}>
          <button onClick={() => setPaymentModal(true)} style={{ flex: 1, background: "#16a34a", color: "#fff", border: "none", borderRadius: 10, padding: "10px", fontSize: 13, fontWeight: 600, cursor: "pointer" }}>💰 Add Payment</button>
          <button onClick={() => { setBillTemplate("fancy"); setShowBill(true); }} style={{ flex: 1, background: "#1e3a5f", color: "#fff", border: "none", borderRadius: 10, padding: "10px", fontSize: 13, fontWeight: 600, cursor: "pointer" }}>📄 Generate Bill</button>
        </div>

        <div style={{ padding: "0 16px 16px" }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "#64748b", marginBottom: 10, letterSpacing: 0.5 }}>TRANSACTION HISTORY</div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {txns.map(t => (
              <div key={t.id} style={{ background: "#fff", border: "1px solid #e2e8f0", borderRadius: 12, padding: "12px 14px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <div style={{ fontSize: 20 }}>{t.type === "lend" ? "🛒" : "💵"}</div>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, color: "#1e293b" }}>
                      {t.type === "lend" ? `${t.item} × ${t.qty} ${t.unit}` : `Payment Received`}
                    </div>
                    <div style={{ fontSize: 12, color: "#94a3b8" }}>{formatDate(t.date)}{t.type === "lend" ? ` • ₹${t.price}/${t.unit}` : ""}</div>
                  </div>
                </div>
                <div style={{ fontSize: 15, fontWeight: 700, color: t.type === "lend" ? "#dc2626" : "#16a34a" }}>
                  {t.type === "lend" ? `-₹${t.total}` : `+₹${t.amount}`}
                </div>
              </div>
            ))}
            {txns.length === 0 && <div style={{ textAlign: "center", color: "#94a3b8", padding: 24 }}>No transactions yet</div>}
          </div>
        </div>
      </div>
    );
  };

  const VoiceScreen = () => (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", padding: 16 }}>
      <div style={{ background: "#f8fafc", borderRadius: 16, padding: 20, marginBottom: 16, border: "1px solid #e2e8f0" }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: "#64748b", marginBottom: 8 }}>💡 EXAMPLES (Hindi/English mix)</div>
        {VOICE_EXAMPLES.slice(0, 3).map((e, i) => (
          <div key={i} style={{ fontSize: 13, color: "#475569", padding: "4px 0", borderBottom: i < 2 ? "1px dashed #e2e8f0" : "none" }}>"{e}"</div>
        ))}
      </div>

      {/* Big Mic Button */}
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
        <div style={{ position: "relative", marginBottom: 24 }}>
          {isListening && (
            <>
              {[80, 100, 120].map((s, i) => (
                <div key={i} style={{
                  position: "absolute", top: "50%", left: "50%",
                  width: s + 40, height: s + 40, borderRadius: "50%",
                  background: `rgba(99,102,241,${0.15 - i * 0.04})`,
                  transform: "translate(-50%,-50%)",
                  animation: `pulse ${1 + i * 0.3}s ease-in-out infinite`,
                }} />
              ))}
            </>
          )}
          <button
            onClick={simulateVoice}
            style={{
              width: 100, height: 100, borderRadius: 50,
              background: isListening ? "linear-gradient(135deg,#ef4444,#dc2626)" : "linear-gradient(135deg,#7c3aed,#4f46e5)",
              border: "none", cursor: "pointer", fontSize: 40,
              boxShadow: isListening ? "0 0 0 4px rgba(239,68,68,0.3)" : "0 8px 32px rgba(99,102,241,0.4)",
              transition: "all 0.3s", position: "relative", zIndex: 2,
            }}
          >
            {isListening ? "🔴" : "🎙️"}
          </button>
        </div>

        <div style={{ fontSize: 16, fontWeight: 600, color: "#1e293b", marginBottom: 8 }}>
          {voiceStatus === "idle" && "Tap mic to speak"}
          {voiceStatus === "listening" && "Listening..."}
          {voiceStatus === "processing" && "Processing..."}
          {voiceStatus === "done" && "Got it! Review below"}
        </div>
        <div style={{ fontSize: 13, color: "#94a3b8" }}>Tap to simulate voice input</div>
      </div>

      {voiceText && (
        <div style={{ background: "#f8fafc", border: "1px solid #e2e8f0", borderRadius: 14, padding: 16, marginBottom: 12 }}>
          <div style={{ fontSize: 12, color: "#94a3b8", marginBottom: 6 }}>HEARD:</div>
          <div style={{ fontSize: 15, color: "#1e293b", fontStyle: "italic" }}>"{voiceText}"</div>
        </div>
      )}

      {parsed && voiceStatus === "done" && (
        <div style={{ background: "#fff", border: "2px solid #7c3aed", borderRadius: 14, padding: 16, marginBottom: 12 }}>
          <div style={{ fontSize: 12, color: "#7c3aed", fontWeight: 600, marginBottom: 10 }}>✅ UNDERSTOOD:</div>
          <div style={{ fontSize: 13, display: "flex", flexDirection: "column", gap: 6 }}>
            {parsed.type === "lend" && <>
              <div>👤 Customer: <b>{parsed.customerName || "Unknown"}</b></div>
              <div>🛒 Item: <b>{parsed.item || "?"}</b> × {parsed.qty || "?"} {parsed.unit || ""}</div>
              <div>💰 Rate: <b>₹{parsed.price || "?"}</b> → Total: <b>₹{(parsed.qty || 0) * (parsed.price || 0)}</b></div>
            </>}
            {parsed.type === "payment" && <>
              <div>👤 Customer: <b>{parsed.customerName || "Unknown"}</b></div>
              <div>💵 Payment: <b>₹{parsed.amount}</b></div>
            </>}
            {parsed.type === "new_customer" && <>
              <div>👤 New Customer: <b>{parsed.customerName}</b></div>
              <div>📞 Phone: <b>{parsed.phone || "N/A"}</b></div>
            </>}
          </div>
          <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
            <button onClick={() => { setParsed(null); setVoiceText(""); setVoiceStatus("idle"); }} style={{ flex: 1, background: "#f3f4f6", border: "none", borderRadius: 8, padding: 10, cursor: "pointer", fontWeight: 600 }}>✗ Cancel</button>
            <button onClick={confirmVoiceAction} style={{ flex: 1, background: "#7c3aed", color: "#fff", border: "none", borderRadius: 8, padding: 10, cursor: "pointer", fontWeight: 600 }}>✓ Confirm Save</button>
          </div>
        </div>
      )}
      <style>{`@keyframes pulse { 0%,100%{transform:translate(-50%,-50%) scale(1);opacity:1} 50%{transform:translate(-50%,-50%) scale(1.1);opacity:0.7} }`}</style>
    </div>
  );

  const BillScreen = () => (
    <div style={{ flex: 1, overflowY: "auto", padding: 16 }}>
      <div style={{ fontSize: 13, fontWeight: 600, color: "#64748b", marginBottom: 10, letterSpacing: 0.5 }}>SELECT TEMPLATE</div>
      <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
        {BILL_TEMPLATES.map(t => (
          <button key={t.id} onClick={() => setBillTemplate(t.id)} style={{ flex: 1, background: billTemplate === t.id ? "#1e3a5f" : "#f1f5f9", color: billTemplate === t.id ? "#fff" : "#1e293b", border: "none", borderRadius: 10, padding: "10px 4px", cursor: "pointer", fontSize: 12, fontWeight: 600 }}>
            <div style={{ fontSize: 20 }}>{t.icon}</div>{t.name}
          </button>
        ))}
      </div>
      <div style={{ fontSize: 13, fontWeight: 600, color: "#64748b", marginBottom: 10, letterSpacing: 0.5 }}>SELECT CUSTOMER</div>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        {db.customers.map(c => {
          const out = getOutstanding(c.id, db.transactions);
          return (
            <button key={c.id} onClick={() => { setSelectedCustomer(c); setShowBill(true); }} style={{ background: "#fff", border: "1px solid #e2e8f0", borderRadius: 12, padding: "12px 14px", display: "flex", alignItems: "center", justifyContent: "space-between", cursor: "pointer" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <div style={{ width: 36, height: 36, borderRadius: 18, background: `hsl(${c.id * 60},60%,85%)`, display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 700, color: `hsl(${c.id * 60},60%,35%)` }}>{c.name[0]}</div>
                <div style={{ textAlign: "left" }}>
                  <div style={{ fontSize: 14, fontWeight: 600 }}>{c.name}</div>
                  <div style={{ fontSize: 12, color: "#94a3b8" }}>Outstanding: ₹{out}</div>
                </div>
              </div>
              <div style={{ fontSize: 22 }}>📄</div>
            </button>
          );
        })}
      </div>
    </div>
  );

  return (
    <div style={{ maxWidth: 390, margin: "0 auto", height: "100vh", display: "flex", flexDirection: "column", background: "#f8fafc", fontFamily: "'Noto Sans', 'Segoe UI', sans-serif", position: "relative", overflow: "hidden" }}>
      {/* Status bar */}
      <div style={{ background: "#0f2540", color: "#fff", padding: "8px 16px 4px", display: "flex", justifyContent: "space-between", fontSize: 11 }}>
        <span>9:41 AM</span><span>📶 🔋</span>
      </div>

      {/* App Header */}
      <div style={{ background: "#0f2540", color: "#fff", padding: "8px 16px 12px", display: "flex", alignItems: "center", gap: 10 }}>
        {screen !== "home" && (
          <button onClick={() => setScreen("home")} style={{ background: "none", border: "none", color: "#fff", fontSize: 20, cursor: "pointer", padding: 0 }}>‹</button>
        )}
        <div style={{ flex: 1, fontSize: 16, fontWeight: 700 }}>
          {screen === "home" && "🏪 Udhar Khata"}
          {screen === "customer" && (selectedCustomer?.name || "Customer")}
          {screen === "voice" && "🎙️ Voice Entry"}
          {screen === "bill" && "📄 Generate Bill"}
        </div>
        {screen === "home" && (
          <button onClick={() => setScreen("voice")} style={{ background: "rgba(255,255,255,0.15)", border: "none", color: "#fff", borderRadius: 8, padding: "6px 12px", fontSize: 13, cursor: "pointer", fontWeight: 600 }}>🎙️ Speak</button>
        )}
      </div>

      {/* Screen Content */}
      {screen === "home" && <HomeScreen />}
      {screen === "customer" && <CustomerScreen />}
      {screen === "voice" && <VoiceScreen />}
      {screen === "bill" && <BillScreen />}

      {/* Bottom Nav */}
      <div style={{ background: "#fff", borderTop: "1px solid #e2e8f0", display: "flex", padding: "8px 0 4px" }}>
        {[
          { icon: "🏠", label: "Home", s: "home" },
          { icon: "🎙️", label: "Voice", s: "voice" },
          { icon: "👥", label: "Customers", s: "customers" },
          { icon: "📄", label: "Bills", s: "bill" },
        ].map(n => (
          <button key={n.s} onClick={() => { if (n.s === "customers") setScreen("home"); else setScreen(n.s); }} style={{ flex: 1, background: "none", border: "none", cursor: "pointer", display: "flex", flexDirection: "column", alignItems: "center", gap: 2, padding: "4px 0" }}>
            <span style={{ fontSize: 20 }}>{n.icon}</span>
            <span style={{ fontSize: 10, color: screen === n.s ? "#7c3aed" : "#94a3b8", fontWeight: screen === n.s ? 700 : 400 }}>{n.label}</span>
          </button>
        ))}
      </div>

      {/* Modals */}
      {showBill && selectedCustomer && (
        <BillModal customer={selectedCustomer} transactions={db.transactions} template={billTemplate} onClose={() => setShowBill(false)} />
      )}

      {addCustomerModal && (
        <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "flex-end" }}>
          <div style={{ background: "#fff", width: "100%", borderRadius: "20px 20px 0 0", padding: 24 }}>
            <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>Add New Customer</div>
            <input value={newCustName} onChange={e => setNewCustName(e.target.value)} placeholder="Customer Name *" style={{ width: "100%", padding: "12px", border: "1px solid #e2e8f0", borderRadius: 10, fontSize: 14, marginBottom: 10, boxSizing: "border-box" }} />
            <input value={newCustPhone} onChange={e => setNewCustPhone(e.target.value)} placeholder="Phone Number" type="tel" style={{ width: "100%", padding: "12px", border: "1px solid #e2e8f0", borderRadius: 10, fontSize: 14, marginBottom: 16, boxSizing: "border-box" }} />
            <div style={{ display: "flex", gap: 8 }}>
              <button onClick={() => setAddCustomerModal(false)} style={{ flex: 1, padding: 12, background: "#f3f4f6", border: "none", borderRadius: 10, cursor: "pointer", fontWeight: 600 }}>Cancel</button>
              <button onClick={addCustomer} style={{ flex: 1, padding: 12, background: "#1e3a5f", color: "#fff", border: "none", borderRadius: 10, cursor: "pointer", fontWeight: 600 }}>Add Customer</button>
            </div>
          </div>
        </div>
      )}

      {paymentModal && selectedCustomer && (
        <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "flex-end" }}>
          <div style={{ background: "#fff", width: "100%", borderRadius: "20px 20px 0 0", padding: 24 }}>
            <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 4 }}>Add Payment</div>
            <div style={{ fontSize: 13, color: "#64748b", marginBottom: 16 }}>Outstanding: ₹{getOutstanding(selectedCustomer.id, db.transactions)}</div>
            <input value={payAmount} onChange={e => setPayAmount(e.target.value)} placeholder="Amount received (₹)" type="number" style={{ width: "100%", padding: "12px", border: "1px solid #e2e8f0", borderRadius: 10, fontSize: 18, fontWeight: 700, marginBottom: 16, boxSizing: "border-box", textAlign: "center" }} />
            <div style={{ display: "flex", gap: 8 }}>
              <button onClick={() => setPaymentModal(false)} style={{ flex: 1, padding: 12, background: "#f3f4f6", border: "none", borderRadius: 10, cursor: "pointer", fontWeight: 600 }}>Cancel</button>
              <button onClick={addPayment} style={{ flex: 1, padding: 12, background: "#16a34a", color: "#fff", border: "none", borderRadius: 10, cursor: "pointer", fontWeight: 600 }}>💰 Save Payment</button>
            </div>
          </div>
        </div>
      )}

      {/* Toast */}
      {toast && (
        <div style={{ position: "absolute", top: 80, left: 16, right: 16, background: toast.type === "success" ? "#16a34a" : "#dc2626", color: "#fff", borderRadius: 12, padding: "12px 16px", fontSize: 14, fontWeight: 600, zIndex: 200, boxShadow: "0 4px 20px rgba(0,0,0,0.2)", animation: "slideIn 0.3s ease" }}>
          ✅ {toast.msg}
        </div>
      )}
      <style>{`@keyframes slideIn{from{transform:translateY(-20px);opacity:0}to{transform:translateY(0);opacity:1}}`}</style>
    </div>
  );
}
