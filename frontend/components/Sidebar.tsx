import React from 'react';
import Link from 'next/link';
import { LayoutDashboard, Radio, Sliders, Utensils, MessageSquare, FileText } from 'lucide-react';

export default function Sidebar() {
  const navItems = [
    { name: 'Dashboard', href: '/', icon: LayoutDashboard },
    { name: 'Wearable Simulator', href: '/simulator', icon: Radio },
    { name: 'What-If Simulations', href: '/simulations', icon: Sliders },
    { name: 'Nutrition & Glucose', href: '/nutrition', icon: Utensils },
    { name: 'Twin AI Coach', href: '/chat', icon: MessageSquare },
    { name: 'Lab Reports', href: '/reports', icon: FileText },
  ];

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col min-h-[calc(100vh-73px)]">
      <div className="p-4 flex-1 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <Link
              key={item.name}
              href={item.href}
              className="flex items-center space-x-3 px-4 py-3 rounded-xl text-slate-300 hover:bg-slate-800 hover:text-cyan-400 transition-colors font-medium text-sm"
            >
              <Icon className="h-5 w-5" />
              <span>{item.name}</span>
            </Link>
          );
        })}
      </div>
      <div className="p-4 border-t border-slate-800 text-xs text-slate-500">
        Digital Twin v1.0.0
      </div>
    </aside>
  );
}
