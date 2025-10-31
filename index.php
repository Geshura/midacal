<!DOCTYPE html>
<html lang="pl">
<head>
<meta charset="UTF-8">
<title>Kalendarz Google Style</title>
<style>
/* --- Typografia i reset --- */
body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    margin: 0;
    padding: 20px;
    background: #f4f6f8;
    color: #333;
    transition: background 0.3s, color 0.3s;
}
h1 { text-align: center; margin-bottom: 10px; }

/* --- Dark Mode --- */
body.dark { background: #121212; color: #eee; }
body.dark .calendar { background: #333; }
body.dark .day { background: #1e1e1e; border-color: #444; }
body.dark .event { background-color: #bb86fc; color: #121212; }
body.dark .modal-content { background: #1e1e1e; color: #eee; }
body.dark input, body.dark textarea { background: #333; color: #eee; border-color: #555; }
body.dark .event-list { background: #1e1e1e; color: #eee; }
body.dark .event-item { border-color: #444; }

/* --- Pasek DarkMode i nawigacji --- */
#darkModeToggle { display:block; margin:0 auto 15px auto; padding:8px 15px; border:none; border-radius:5px; cursor:pointer; background:#1a73e8; color:white; font-weight:bold; transition: background 0.2s; }
#darkModeToggle:hover { background:#1669c1; }

.navbar { display: flex; justify-content: center; align-items: center; gap: 15px; margin-bottom: 10px; }
.navbar span {
    font-weight: bold;
    font-size: 18px;
    min-width: 150px; /* stała szerokość boxa miesiąca */
    text-align: center;
    display: inline-block;
}
.navbar button { padding:6px 12px; border:none; border-radius:5px; cursor:pointer; background:#1a73e8; color:white; font-weight:bold; transition: background 0.2s; }
.navbar button:hover { background:#1669c1; }

/* --- Widok przełączania miesiąc/tydzień --- */

/* --- Kontener kalendarza --- */
.calendar {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 2px;
    background: #e0e0e0;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
    transition: background 0.3s;
}

/* --- Każdy dzień --- */
.day {
    background: #fff;
    min-height: 100px;
    padding: 5px;
    border-radius: 4px;
    position: relative;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s, background 0.3s;
}
.day:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.day.today { border: 2px solid #1a73e8; }

/* --- Data i wydarzenia --- */
.day .date { font-weight:600; margin-bottom:5px; }
.event { background-color:#1a73e8; color:white; padding:3px 6px; border-radius:4px; font-size:12px; margin-top:3px; box-shadow:0 1px 2px rgba(0,0,0,0.2); transition: background 0.3s, color 0.3s; }

/* --- Lista wydarzeń / harmonogram --- */
.event-list { margin-top:20px; background:#fff; padding:15px; border-radius:8px; box-shadow:0 4px 10px rgba(0,0,0,0.1); transition: background 0.3s, color 0.3s; }
.event-item { border-bottom:1px solid #ddd; padding:8px 0; }
.event-item:last-child { border-bottom:none; }

/* --- Modal --- */
.modal { display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.6); justify-content:center; align-items:center; z-index:100; }
.modal-content { background:#fff; padding:25px; border-radius:10px; width:350px; box-shadow:0 6px 20px rgba(0,0,0,0.25); animation:fadeIn 0.3s ease; transition: background 0.3s,color 0.3s; }
.modal h3 { margin-top:0; }
.modal input, .modal textarea { width:100%; margin-bottom:10px; padding:8px; border-radius:5px; border:1px solid #ccc; font-size:14px; transition:background 0.3s,color 0.3s,border-color 0.3s; }
.modal button { padding:8px 15px; border:none; border-radius:5px; font-weight:bold; cursor:pointer; transition:background 0.2s; }
#saveEvent { background-color:#1a73e8; color:white; }
#saveEvent:hover { background-color:#1669c1; }
.modal button:nth-child(5) { background:#e0e0e0; margin-left:10px; }
.modal button:nth-child(5):hover { background:#cfcfcf; }

@keyframes fadeIn { from{opacity:0;transform:translateY(-20px);} to{opacity:1;transform:translateY(0);} }
</style>
</head>
<body>

<h1>Kalendarz Google Style</h1>
<button id="darkModeToggle">Przełącz Dark Mode</button>

<div class="navbar">
    <button id="prevMonth">&lt;</button>
    <span id="monthYear">Wrzesień 2025</span>
    <button id="nextMonth">&gt;</button>
</div>

<div class="view-toggle">
    <button id="monthView" class="active">Miesiąc</button>
    <button id="weekView">Tydzień</button>
</div>

<div class="calendar"></div>

<div class="event-list" id="eventList">
    <h3>Harmonogram</h3>
</div>

<div class="modal" id="eventModal">
    <div class="modal-content">
        <h3>Dodaj Wydarzenie</h3>
        <input type="text" id="title" placeholder="Tytuł">
        <textarea id="description" placeholder="Opis"></textarea>
        <input type="datetime-local" id="start">
        <input type="datetime-local" id="end">
        <div style="text-align:right;">
            <button id="saveEvent">Zapisz</button>
            <button onclick="closeModal()">Anuluj</button>
        </div>
    </div>
</div>

<script>
const calendarEl = document.querySelector('.calendar');
const modal = document.getElementById('eventModal');
const darkToggle = document.getElementById('darkModeToggle');
const monthYearEl = document.getElementById('monthYear');
const eventListEl = document.getElementById('eventList');
const monthViewBtn = document.getElementById('monthView');
const weekViewBtn = document.getElementById('weekView');
let selectedDate = null;

let today = new Date();
let currentMonth = today.getMonth();
let currentYear = today.getFullYear();
let currentView = 'month';

/* --- Dark Mode --- */
darkToggle.addEventListener('click', () => document.body.classList.toggle('dark'));

/* --- Nawigacja miesięcy --- */
document.getElementById('prevMonth').addEventListener('click', () => { 
    currentMonth--; 
    if(currentMonth < 0){ currentMonth = 11; currentYear--; } 
    loadCalendar(currentMonth,currentYear); 
});
document.getElementById('nextMonth').addEventListener('click', () => { 
    currentMonth++; 
    if(currentMonth > 11){ currentMonth = 0; currentYear++; } 
    loadCalendar(currentMonth,currentYear); 
});

/* --- Przełączanie widoku --- */
monthViewBtn.addEventListener('click', () => { currentView='month'; monthViewBtn.classList.add('active'); weekViewBtn.classList.remove('active'); loadCalendar(currentMonth,currentYear); });
weekViewBtn.addEventListener('click', () => { currentView='week'; monthViewBtn.classList.remove('active'); weekViewBtn.classList.add('active'); loadCalendar(currentMonth,currentYear); });

/* --- Funkcja do nazwy miesiąca --- */
function getMonthName(month){ 
    return ['Styczeń','Luty','Marzec','Kwiecień','Maj','Czerwiec','Lipiec','Sierpień','Wrzesień','Październik','Listopad','Grudzień'][month]; 
}

/* --- Ładowanie kalendarza --- */
function loadCalendar(month,year){
    calendarEl.innerHTML='';
    monthYearEl.textContent=`${getMonthName(month)} ${year}`;
    const firstDay = new Date(year,month,1).getDay();
    const lastDate = new Date(year,month+1,0).getDate();
    const todayDate = new Date();

    if(currentView==='month'){
        calendarEl.style.gridTemplateColumns='repeat(7,1fr)';
        for(let i=0;i<firstDay;i++){ const emptyCell=document.createElement('div'); emptyCell.classList.add('day'); calendarEl.appendChild(emptyCell);}
        for(let d=1;d<=lastDate;d++){
            const dayEl=document.createElement('div');
            dayEl.classList.add('day');
            dayEl.innerHTML=`<div class="date">${d}</div>`;
            if(year===todayDate.getFullYear() && month===todayDate.getMonth() && d===todayDate.getDate()) dayEl.classList.add('today');
            dayEl.addEventListener('click',()=>openModal(year,month+1,d));
            calendarEl.appendChild(dayEl);
        }
    }else{
        // Widok tygodnia: pokaż bieżący tydzień
        const startOfWeek=new Date(todayDate.setDate(todayDate.getDate()-todayDate.getDay()));
        calendarEl.style.gridTemplateColumns='repeat(7,1fr)';
        for(let i=0;i<7;i++){
            const d=new Date(startOfWeek); d.setDate(startOfWeek.getDate()+i);
            const dayEl=document.createElement('div');
            dayEl.classList.add('day');
            dayEl.innerHTML=`<div class="date">${d.getDate()} ${getMonthName(d.getMonth())}</div>`;
            if(d.toDateString()===new Date().toDateString()) dayEl.classList.add('today');
            dayEl.addEventListener('click',()=>openModal(d.getFullYear(),d.getMonth()+1,d.getDate()));
            calendarEl.appendChild(dayEl);
        }
    }

    loadEvents(month,year);
}

/* --- Pobranie wydarzeń --- */
function loadEvents(month,year){
    const startDate=`${year}-${month+1}-01`;
    const endDate=new Date(year,month+1,0);
    const endDateStr=`${endDate.getFullYear()}-${endDate.getMonth()+1}-${endDate.getDate()}`;
    fetch(`get_events.php?start=${startDate}&end=${endDateStr}`).then(res=>res.json()).then(events=>{
        // Kalendarz
        const firstDay=new Date(year,month,1).getDay();
        events.forEach(ev=>{
            const date=new Date(ev.start_datetime);
            const dayCell=currentView==='month'?calendarEl.children[date.getDate()+firstDay-1]:[...calendarEl.children].find(c=>c.querySelector('.date') && c.querySelector('.date').textContent.startsWith(date.getDate()));
            if(dayCell){
                const evEl=document.createElement('div');
                evEl.classList.add('event');
                evEl.textContent=ev.title;
                dayCell.appendChild(evEl);
            }
        });

        // Lista wydarzeń pod kalendarzem
        eventListEl.innerHTML='<h3>Harmonogram</h3>';
        const sortedEvents=events.sort((a,b)=>new Date(a.start_datetime)-new Date(b.start_datetime));
        sortedEvents.forEach(ev=>{
            const evItem=document.createElement('div');
            evItem.classList.add('event-item');
            evItem.innerHTML=`<strong>${ev.title}</strong><br>
                ${ev.description||''}<br>
                ${ev.start_datetime} - ${ev.end_datetime}`;
            eventListEl.appendChild(evItem);
        });
    });
}

/* --- Modal --- */
function openModal(year,month,day){
    selectedDate=`${year}-${String(month).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
    modal.style.display='flex';
    document.getElementById('start').value=selectedDate+"T09:00";
    document.getElementById('end').value=selectedDate+"T10:00";
}
function closeModal(){ modal.style.display='none'; }

/* --- Zapis wydarzenia --- */
document.getElementById('saveEvent').addEventListener('click',()=>{
    const title=document.getElementById('title').value;
    const description=document.getElementById('description').value;
    const start=document.getElementById('start').value;
    const end=document.getElementById('end').value;
    fetch('add_event.php',{
        method:'POST',
        headers:{'Content-Type':'application/x-www-form-urlencoded'},
        body:`title=${encodeURIComponent(title)}&description=${encodeURIComponent(description)}&start_datetime=${start}&end_datetime=${end}`
    }).then(()=>{
        closeModal();
        loadCalendar(currentMonth,currentYear);
    });
});

loadCalendar(currentMonth,currentYear);
</script>

</body>
</html>
